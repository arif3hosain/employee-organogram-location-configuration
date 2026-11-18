# PMS POC: Angular front end

This is the Angular 22 UI for the PMS POC backend in `../` (Spring Boot). It uses standalone components, lazy-loaded routes, signals and Angular Material, and has no security.

## Run

Prerequisites: Node 20.19+ or 22+, and npm.

```bash
# 1. start the backend first (from the pmspoc folder): mvn spring-boot:run   -> http://localhost:8080
# 2. then, in this folder:
npm install
npm start            # = ng serve, opens on http://localhost:4200
```

`proxy.conf.json` forwards `/api/*` from the dev server to `http://localhost:8080`, so the backend needs no CORS setup.
For a production build, run `npm run build` (output goes to `dist/frontend/browser`). Serve it from the same origin as the API, or add CORS on the backend.

Fonts (Roboto and Material Symbols) are bundled from npm, so the UI doesn't need Google Fonts to be reachable.

## Screens

| Route | What it does |
|---|---|
| `/employees` | Paged list with search and filters (org unit and location filters include everything below), create/edit dialog (supervisor autocomplete) and delete |
| `/organogram` | Org chart (boxes coloured by FUNCTIONAL / NON_FUNCTIONAL) or tree view. Clicking a unit shows its details, add/edit/move/delete, a link to its employees, and the **sanctioned posts** table (sanctioned / filled / vacancy, edit counts inline) |
| `/locations` | Type-wise location tree: add child, edit/move, delete, link to employees at that location and below |
| `/setup` | Location types, org unit types (level numbers) and designations (grade rank) |

## How it talks to the backend

- `core/api.service.ts` is one typed client. Every response is the backend's `Result { success, message, data }`. Reads return `data`, and writes also show `message` in a green snackbar.
- `core/error.interceptor.ts` shows the backend's error message in a red snackbar. That covers a failed `preCondition` (400), a business rule (409) and not found (404). Forms stay open so the user can fix the input.
- The client only checks required fields and email format. The real rules (duplicates, level rules, reporting loops, stale version) come from the backend's Action/Service, which is the point of the POC.
- `core/models.ts` mirrors the Java DTO records. Update it when a DTO changes.

## Layout

```
src/app/
 ├── core/        api.service, models, error.interceptor, notify.service, lookups (tree → dropdown options)
 ├── shared/      tree-view (recursive tree), tree-utils, confirm-dialog
 └── features/
      ├── employees/   employee-list, employee-form-dialog
      ├── organogram/  organogram-page, org-chart, org-unit-form-dialog
      ├── locations/   locations-page, location-form-dialog
      └── setup/       setup-page, setup-item-dialog
```
