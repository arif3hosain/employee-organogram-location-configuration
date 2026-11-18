# PMS POC

A proof of concept for the new PMS backend (Spring Boot 3.5, Java 21, PostgreSQL). It covers:

- the **Controller → Action (preCondition / execute / postCondition) → Service** flow from the PMS Technical Migration Guide §3.4;
- **type-wise locations**: one configurable tree instead of separate Division / District / Upazila tables, loaded with all of Bangladesh (8 divisions, 64 districts, 495 upazilas, BBS geocodes);
- the **organogram**: org unit tree (Organization → Function → Department → Unit → Team) with FUNCTIONAL / NON_FUNCTIONAL nature, **grades**, designations (each linked to a grade) and sanctioned posts;
- **employee CRUD**.

There's no security in this POC. Progress and open questions are tracked in [POC_NOTES.md](POC_NOTES.md).

The **Angular UI** is in [`frontend/`](frontend/README.md). Start the backend, then run `cd frontend && npm install && npm start` and open http://localhost:4200.

## Run

Prerequisites: JDK 21, Maven 3.9+, and PostgreSQL on `localhost:5433` with a database named `pmspoc` (user `postgres`, password `postgres`).

```bash
# once, if the database doesn't exist yet
psql -h localhost -p 5433 -U postgres -c "CREATE DATABASE pmspoc"

mvn spring-boot:run
```

Flyway creates the tables (`V1__corehr_schema.sql`), loads sample data (`V2__corehr_seed_data.sql`) and adds the Grade entity (`V3__grade_entity.sql`) and the Bangladesh divisions, districts and upazilas (`V4__bangladesh_admin_locations.sql`) on start.
To use other connection details, set `DB_URL`, `DB_USERNAME` and `DB_PASSWORD`.

- Swagger UI: http://localhost:8080/swagger-ui.html
- Health: http://localhost:8080/actuator/health

Tests use in-memory H2 in PostgreSQL mode, so they don't need the database:

```bash
mvn test
```

## Code layout

```
com.bracits.pms
 ├── shared/                  BaseAction, Result, BaseEntity, HierarchyPath, error handling, NotificationService (logs only)
 └── corehr/
      ├── location/           LocationType + Location tree
      ├── organogram/         OrgUnitType, OrgUnit tree, Designation, OrgUnitPost (sanctioned posts)
      └── employee/           Employee
          each one: api/ (controller + DTOs) · action/ · service/ · domain/ · infrastructure/ (repositories)
```

`ArchitectureTest` (ArchUnit) fails the build if a layer rule is broken. For example, it fails if an action is `@Transactional`, a controller calls a service, an action uses a repository, or a service sends mail.

## The flow, using "create employee" as the example

```
POST /api/employees
  EmployeeController.create()          -> createEmployeeAction.run(form)          (no logic in the controller)
    1. preCondition(form)              -> format checks + "exists?" / "duplicate?" via services
                                          error? -> 400 {"success":false,"message":...}, nothing saved
    2. execute(form)                   -> employeeService.create(form)              (@Transactional, commits here)
                                          rule broken inside the tx? -> BusinessException -> rollback -> 409
    3. postCondition(form, view)       -> notificationService.send(...)             (only after a successful commit)
  <- 201 {"success":true,"message":"Employee E0100 created","data":{...}}
```

## API overview

| Resource | Endpoints |
|---|---|
| Location types | `GET/POST /api/location-types`, `PUT/DELETE /api/location-types/{id}` |
| Locations | `GET /api/locations?typeId=&parentId=&q=`, `GET /api/locations/tree?rootId=`, `GET/PUT/DELETE /api/locations/{id}`, `POST /api/locations` |
| Org unit types | `GET/POST /api/org-unit-types`, `PUT/DELETE /api/org-unit-types/{id}` |
| Org units | `GET /api/org-units?typeId=&parentId=&nature=&q=`, `GET /api/org-units/tree?rootId=`, `GET/PUT/DELETE /api/org-units/{id}`, `POST /api/org-units` |
| Sanctioned posts | `GET/PUT /api/org-units/{id}/posts`, `DELETE /api/org-units/posts/{postId}` |
| Grades | `GET/POST /api/grades`, `PUT/DELETE /api/grades/{id}` |
| Designations | `GET/POST /api/designations`, `PUT/DELETE /api/designations/{id}` (body has `gradeId`) |
| Employees | `GET /api/employees?q=&orgUnitId=&locationId=&designationId=&status=&page=&size=`, `GET/PUT/DELETE /api/employees/{id}`, `POST /api/employees` |

`orgUnitId` and `locationId` on the employee search include the whole subtree. For example, `?orgUnitId=2` returns everyone anywhere under the Technology function.

### Try it

```bash
# location tree
curl -s localhost:8080/api/locations/tree | jq

# organogram chart, and the sanctioned vs filled posts of the ERP Unit
curl -s localhost:8080/api/org-units/tree | jq
curl -s localhost:8080/api/org-units/8/posts | jq

# create an employee (designation 6 = Officer, org unit 8 = ERP Unit, location 8 = Mirpur-10 Branch, supervisor 7 = E0004)
curl -s -X POST localhost:8080/api/employees -H 'Content-Type: application/json' -d '{
  "employeeCode":"E0100","fullName":"New Person","email":"new.person@example.com",
  "joinDate":"2025-01-01","designationId":6,"orgUnitId":8,"locationId":8,"supervisorId":7}' | jq

# a failed preCondition returns 400 and nothing is saved
curl -s -X POST localhost:8080/api/employees -H 'Content-Type: application/json' -d '{"employeeCode":"X"}' | jq

# update: send back the version you loaded, or you'll get 409
curl -s -X PUT localhost:8080/api/employees/9 -H 'Content-Type: application/json' -d '{
  "employeeCode":"E0100","fullName":"New Person Renamed","email":"new.person@example.com",
  "joinDate":"2025-01-01","designationId":6,"orgUnitId":8,"locationId":8,"supervisorId":7,"version":0}' | jq
```

## HTTP status rules

| Status | When |
|---|---|
| 200 / 201 | Success |
| 400 | `preCondition` failed (validation, not found, duplicate), or the JSON is malformed |
| 404 | A record went missing during `execute` |
| 409 | A business rule broke inside the service transaction (level rule, move into own subtree, reporting loop, stale `version`, still in use) |
