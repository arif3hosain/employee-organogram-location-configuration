# PMS POC: progress and open questions

How to run it: see [README.md](README.md).
Reference documents: `sbicloud-bd-grails-app/Doc/pms-migration/PMS_Technical_Migration_Guide.md` (§2.3 package layout, §3.4 request flow) and `CLAUDE.md`.

---

## Questions for you

Each question lists what the POC does today as **Current**. Answer inline, and the code will follow.

### Scope and direction

**Q1. Who owns employee, org and location data: PMS or the ERP?**
Migration principle 3 says corehr is a *read-only replica* synced from the ERP (`CoreHrPort`). This POC gives PMS full CRUD, as you asked.
- Is the plan that the **sellable product owns** this data (standalone customers), while the **BRAC deployment syncs** it from the ERP?
- Or is CRUD only for the POC?

**Current:** full CRUD in PMS, no sync.

**Q2. Database.** You asked for PostgreSQL (`localhost:5433/pmspoc`). CLAUDE.md lists "Oracle vs PostgreSQL for the product edition" as open, with Phase 1 staying on Oracle.
- Does this settle it as **PostgreSQL for the product**?
- Do we still need an Oracle profile for the BRAC deployment?

**Current:** PostgreSQL only. The DDL avoids Postgres-only features (VARCHAR status instead of BOOLEAN, identity columns), so an Oracle port stays cheap.

**Q3. Frontend.** ~~Do you want an Angular UI?~~ **Answered 18 Nov: yes.** It's built in `frontend/`. Still open: the guide lists **Angular Material or PrimeNG**; the POC uses Angular Material. Is that acceptable as the product's component library?

**Q4. Package and product name.** Is `com.bracits.pms` correct? The guide says `com.<vendor>.pms`.

### Organogram

**Q5. What does "function / non-function" mean?** I read it as a **nature** on each org unit:
- `FUNCTIONAL` = core business units (e.g. Technology, Programme);
- `NON_FUNCTIONAL` = support units (e.g. HR, Finance, Admin).

A child unit inherits its parent's nature unless one is set, and the organization root is unclassified. If you meant something else, tell me. For example, "Function" might only be a level type, and that already exists separately as the `FUNCTION` org unit type.

**Q6a. Grade on the employee?** Grades are now an entity, and each designation belongs to one grade. Can an employee hold a **different grade from their designation's** (e.g. a promotion in grade without a title change)? If so, the employee needs their own `grade_id` too.

**Current:** an employee's grade comes from their designation.

**Q6. "design".** I read this as **designation**. I added designations plus **sanctioned posts**: how many of each designation a unit may have, with filled and vacancy counts.
- Is that the organogram you meant?
- Should assigning an employee be **blocked** when a unit has no vacancy for that designation?

**Current:** no block. Saving a post only warns when a unit is over-staffed.

**Q7. Org unit levels.** The current types are ORGANIZATION (1) > FUNCTION (2) > DEPARTMENT (3) > UNIT (4) > TEAM (5). Levels may be skipped (e.g. a Unit directly under a Function).
- Is that correct for BRAC/BITS?
- Do we need a **head of unit** (an employee or designation) on each org unit?

**Q8.** Can one org unit sit in **several locations**?

**Current:** one optional location per unit.

### Location

**Q9. Location types.** ~~Which types does the business use?~~ **Answered 18 Nov:** COUNTRY > DIVISION > DISTRICT > UPAZILA, loaded for all of Bangladesh in V4, with BRANCH/OFFICE at level 5. Still open: Dhaka and Chattogram **city thanas** (Gulshan, Mirpur, Double Mooring…) aren't upazilas and aren't loaded; offices in the cities sit directly under the district. Do you need a THANA level for the city corporations? Also, should locations keep the **Bangla name** (the source data has it)?
- Which types does the business actually use?
- Who will map the legacy Division / District / Thana data and the `PhysicalOfficeInfo` offices onto the new tree?

**Q10. One tree or several?** Some organizations keep an administrative hierarchy and an operational one side by side.

**Current:** one tree. The guide also separates **Office** from **Location**; here an office is just a node of type BRANCH/OFFICE. Is that acceptable?

**Q11.** Do locations need extra attributes, such as an address, GPS coordinates or a phone number?

### Employee

**Q12. Fields.** The POC keeps the minimum: code, name, email, phone, join date, designation, org unit, location, supervisor and status. Which other fields does PMS need? Likely candidates are employee level/grade, category, employment type, gender, confirmation date and HRBP.

**Q13. Reporting line.** Is a single direct supervisor enough? PMS may also need a second-level supervisor, a dotted line, an HRBP, or the assessor-change and delegation features (workflow module).

**Q14. Delete.** Is a hard delete acceptable here, or should delete only set the employee INACTIVE and keep the history?

**Current:** hard delete. It's blocked when the employee supervises someone or the record is still referenced.

**Q15. Optimistic locking.** Should `version` be mandatory on every update?

**Current:** mandatory for employee updates, optional elsewhere.

---

## Design decisions made in the POC (change any you disagree with)

| # | Decision | Why |
|---|---|---|
| D1 | One `BaseAction<I,O>` with `run()` = `preCondition` → `execute` → `postCondition`, returning `Result<T>(success, message, data)` | Guide §3.4. `Result` adds a `data` payload so CRUD can return the saved record. |
| D2 | `postCondition(input, output)` gets the input as well as the output | Mail text often needs both. A small extension to the guide's signature. |
| D3 | Reads go through actions too (`GetXxxAction`, `SearchXxxAction`) | Guide rule: controllers never call services. |
| D4 | Critical checks run twice: in `preCondition` (fast, friendly 400) and again in the service (409) | Guide rule 5: data can change between `preCondition` and the transaction. |
| D5 | Locations and org units are **typed self-referencing trees** with a materialized `path` (`/1/5/12/`) | A new level needs no schema change. Subtree search is a single `LIKE` ("everyone under Technology"), and moves are checked for cycles. |
| D6 | Level rule: a child's type level must be greater than its parent's; skipping levels is allowed | Flexible, without per-type parent rules. |
| D7 | Status is stored as VARCHAR (`ACTIVE`/`INACTIVE`), not BOOLEAN | Keeps the DDL portable to Oracle. |
| D8 | Sub-modules talk through services only (organogram → `LocationService`, employee → organogram and location services) | Guide §2.3. The one exception is the headcount query in `OrgUnitPostRepository`: it reads the `employee` table with native SQL so that organogram doesn't depend on employee. |
| D9 | `open-in-view=false`, and services return DTOs built inside the transaction | No lazy-loading surprises in controllers. |
| D10 | Mail is a logging stub (`NotificationService`) called only from `postCondition` | Guide rule 4. The real notification module comes later. |
| D11 | No MapStruct, Lombok or Spring Modulith yet | Keeps the POC small. Add them when the module count grows. |
| D12 | Tests use H2 in PostgreSQL mode with the same Flyway scripts | Fast and needs no database. Move to Testcontainers (guide §2.2) once Docker is available in CI. |

---

## Progress log

### 19 Nov 2026: drop-down tree for locations

- [x] `frontend/src/app/shared/location-picker.ts` is now a **drop-down tree**. The field opens a panel with the whole location tree (Bangladesh › Division › District › Upazila / Office). Branches expand and collapse, levels are sorted alphabetically, and each node shows its type. Clicking any node at any level picks it.
  - There's a **search box** at the top: typing "mirpur" shows only matches and their parents, expanded, with the match in bold.
  - On opening, the panel expands the way down to the current value. A ✕ clears the choice.
  - In forms the full path shows under the field (Bangladesh › Dhaka › Dhaka › Mirpur-10 Branch). In the list filter the first row is "All locations".
  - An earlier version showed one drop-down per level (Division, then District, then Upazila); you asked for a tree instead, so that was replaced.
- [x] Used in the **employee list filter**, the **employee form** (Workplace, required) and the **org unit form** (optional).
- Tested in headless Chrome against a mock with the real 8/64/495 data: All = 8 employees, Mirpur-10 = 2, search "agra" → Agrabad Branch = 1, "All locations" = 8. The edit form pre-fills the value and shows the path hint.

### 18 Nov 2026 (late night): first run on PostgreSQL, fix

- First real run. The app compiles and starts, and Flyway applies V1→V4.
- **Fixed:** the employee list failed on PostgreSQL with `operator does not exist: character varying ~~ bytea`. Cause: with no filter, `path like (:orgPath || '%')` received a null value, which PostgreSQL types as `bytea`. Now every LIKE pattern is built in the service and is never null (`%` = no filter). The same was done for the location and org-unit search. H2 accepts the null, which is why the tests didn't catch it. Moving the tests to Testcontainers (D12) would catch this class of bug.

### 18 Nov 2026 (night): Bangladesh administrative locations

**Done**
- [x] **`V4__bangladesh_admin_locations.sql`** loads **8 divisions, 64 districts and 495 upazilas** under Bangladesh. Codes are the official **BBS geocodes** (`BD30` Dhaka division, `BD3026` Dhaka district, `BD30260072` Savar). Source: `github.com/nuhil/bangladesh-geocode`. The counts match the current official totals, including newer upazilas such as Eidgaon, Dasar, Madhyanagar and Shantiganj.
- [x] New location types: DIVISION (2), DISTRICT (3), UPAZILA (4). BRANCH/OFFICE moves to level 5.
- [x] The sample branches move under real districts: Head Office and Mirpur-10 under Dhaka, Agrabad under Chattogram. The made-up REGION/AREA sample rows and types are deleted only if nothing uses them. The branch IDs (7, 8, 9) are unchanged, so employees and org units keep their locations.
- [x] Checked on embedded PostgreSQL (V1→V4): all 571 paths are correct, there are no level-rule violations, and the counts are right.
- [x] Integration tests updated: the tree has 8 divisions; there are 64 districts, 495 upazilas and 3 branches; Head Office's parent is Dhaka; the level rule is tested with the new types.
- No code changes were needed: the type-wise design takes the new levels as data.

### 18 Nov 2026 (evening): Grade entity

You asked for grades to be an entity instead of a hand-typed number on designations.

**Done**
- [x] New `grade` table and entity: code, name, `rank_no` (1 = most senior, unique), status. Full CRUD at `/api/grades`, through the same Controller → Action → Service flow.
- [x] `designation.grade_rank` is replaced by `designation.grade_id` (a foreign key to `grade`). A designation now **picks** its grade; its seniority order comes from the grade.
- [x] **`V3__grade_entity.sql`** creates `grade` with G1–G6, links every existing designation to the grade with the same rank, then drops `grade_rank`. It's a new migration rather than an edit of V1/V2, so a database that already ran V1/V2 upgrades in place. Verified on embedded PostgreSQL.
- [x] Rules:
  - grade code and rank must be unique;
  - a grade used by designations can't be deleted (make it INACTIVE instead);
  - an INACTIVE grade can't be given to a designation, but existing links stay;
  - a designation requires a grade.
- [x] Employee and sanctioned-post views now include the grade (`gradeCode`, `gradeName`).
- [x] UI: new **Grades** tab in Setup. The designation dialog has a grade drop-down. The employee list shows the grade under the designation, and the posts table shows the grade code.
- [x] Tests: a new integration test `designationsCarryTheirGrade`; `CreateEmployeeActionTest` updated for the new view fields.

**Verification:** V1→V3 ran on embedded PostgreSQL. The Java was checked with `javac` (all unresolved symbols are Spring/JPA library ones). `ng build` passes, and the screens were checked in headless Chrome against the mock. **`mvn test` on your machine is still the real check.**

### 18 Nov 2026 (later): Angular front end

**Done**
- [x] `frontend/`: Angular 22 (standalone components, signals, lazy routes) with Angular Material. The dev proxy sends `/api` to `:8080`, so no CORS is needed.
- [x] **Employees:** paged list with search, subtree filters by org unit and location, designation and status; create/edit dialog with supervisor autocomplete; delete with confirmation.
- [x] **Organogram:** chart view (boxes coloured FUNCTIONAL / NON_FUNCTIONAL) and tree view; add sub-unit, edit/move, delete; link to the unit's employees; **sanctioned posts** with filled and vacancy counts and inline count edits.
- [x] **Locations:** type-wise tree; add child, edit/move (the parent list excludes the node's own subtree), delete; link to employees.
- [x] **Setup:** location types, org unit types and designations.
- [x] Backend messages are shown as-is: green for success, red for preCondition (400) and business-rule (409) errors.
- [x] Fonts are bundled from npm, so Google Fonts doesn't need to be reachable.

**Verification**
- `ng build`: the development and production builds compile with no errors and no budget warnings (initial bundle 670 kB raw, 144 kB transferred).
- Tested in headless Chrome against a **mock** of the API that uses the same seed data: list, create (a duplicate code shows the server message, then the create succeeds), org chart selection and posts, the org unit → employees link (Technology → 5 rows), the location tree and edit dialog, and setup.
- Not yet run against the real Spring Boot backend. That's the next check, on your machine.

**Next steps**
- Run backend and frontend together. Report any API mismatch: `core/models.ts` mirrors the Java DTOs by hand. Generating it from OpenAPI is an option later.

### 18 Nov 2026: first version

**Done**
- [x] Maven project (Spring Boot 3.5.6, Java 21), PostgreSQL `localhost:5433/pmspoc` as `postgres`/`postgres` (overridable with `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`).
- [x] Shared framework: `BaseAction`, `Result`, `UpdateCommand`, `BaseEntity` (id, `@Version`, timestamps, `assertVersion`), `HierarchyPath`, `GlobalExceptionHandler`, `ApiResponses`, `PageView`, `NotificationService` (stub).
- [x] **Type-wise location:** `LocationType` and `Location` tree. CRUD, search, tree, move with subtree path rebase, level rule, cycle check.
- [x] **Organogram:** `OrgUnitType`, `OrgUnit` (nature FUNCTIONAL/NON_FUNCTIONAL, optional location), `Designation`, `OrgUnitPost` (sanctioned / filled / vacancy). CRUD, tree chart, filter by nature.
- [x] **Employee CRUD:** create, update, delete, get, and paged search with subtree filters by org unit and location. Rules for duplicate code and email, active references, reporting loop, a supervisor can't be deleted or deactivated, stale version.
- [x] Flyway `V1` schema and `V2` sample data (9 locations, 8 org units, 6 designations, 15 post lines, 8 employees).
- [x] Tests:
  - `HierarchyPathTest` (pure);
  - `BaseActionTest` (the three-step contract);
  - `CreateEmployeeActionTest` (Mockito: invalid input means the service is never called; valid input means one call plus mail);
  - `ArchitectureTest` (ArchUnit, the guide's §3.4 rules);
  - `CoreHrFlowIntegrationTest` (MockMvc end to end).
- [x] README with run steps, API table and curl examples.

**Verification status: please read this**
- Both Flyway scripts were run on a real embedded PostgreSQL engine (PGlite). The tables, sample data, paths and generated IDs were all checked.
- The Java code was checked with `javac` for syntax and for references between our own classes. **It has not yet been compiled against Spring or run.** My workspace couldn't download from Maven Central. The first `mvn test` on your machine is the real check; send me any error output.
- If Hibernate schema validation complains at startup, set `spring.jpa.hibernate.ddl-auto: none` in `application.yml` and tell me the message.

**Next steps**
1. You run `mvn test`, then `mvn spring-boot:run`, and try the curl calls in the README.
2. Answer the questions above (Q1, Q3, Q5 and Q6 shape the next iteration most).
3. Then: an Angular POC (if wanted), the extra employee fields, and a sync adapter design behind `CoreHrPort` if Q1 = replica.
