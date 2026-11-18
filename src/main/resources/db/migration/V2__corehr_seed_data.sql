-- =====================================================================
-- PMS POC - sample data. Sample names only; not real people.
-- No explicit ids: identity columns generate them, and references are
-- resolved by code, so the identity sequences stay correct.
-- =====================================================================

-- ---------- Location types (replaces Division / District / Upazila) ----
INSERT INTO location_type (code, name, level_no) VALUES ('COUNTRY', 'Country', 1);
INSERT INTO location_type (code, name, level_no) VALUES ('REGION',  'Region',  2);
INSERT INTO location_type (code, name, level_no) VALUES ('AREA',    'Area',    3);
INSERT INTO location_type (code, name, level_no) VALUES ('BRANCH',  'Branch / Office', 4);

-- ---------- Locations --------------------------------------------------
INSERT INTO location (code, name, location_type_id, parent_id)
VALUES ('BD', 'Bangladesh', (SELECT id FROM location_type WHERE code = 'COUNTRY'), NULL);

INSERT INTO location (code, name, location_type_id, parent_id)
VALUES ('R-DHK', 'Dhaka Region', (SELECT id FROM location_type WHERE code = 'REGION'), (SELECT id FROM location WHERE code = 'BD'));
INSERT INTO location (code, name, location_type_id, parent_id)
VALUES ('R-CTG', 'Chattogram Region', (SELECT id FROM location_type WHERE code = 'REGION'), (SELECT id FROM location WHERE code = 'BD'));

INSERT INTO location (code, name, location_type_id, parent_id)
VALUES ('A-GUL', 'Gulshan Area', (SELECT id FROM location_type WHERE code = 'AREA'), (SELECT id FROM location WHERE code = 'R-DHK'));
INSERT INTO location (code, name, location_type_id, parent_id)
VALUES ('A-MIR', 'Mirpur Area', (SELECT id FROM location_type WHERE code = 'AREA'), (SELECT id FROM location WHERE code = 'R-DHK'));
INSERT INTO location (code, name, location_type_id, parent_id)
VALUES ('A-AGR', 'Agrabad Area', (SELECT id FROM location_type WHERE code = 'AREA'), (SELECT id FROM location WHERE code = 'R-CTG'));

INSERT INTO location (code, name, location_type_id, parent_id)
VALUES ('B-HO', 'Head Office', (SELECT id FROM location_type WHERE code = 'BRANCH'), (SELECT id FROM location WHERE code = 'A-GUL'));
INSERT INTO location (code, name, location_type_id, parent_id)
VALUES ('B-MIR10', 'Mirpur-10 Branch', (SELECT id FROM location_type WHERE code = 'BRANCH'), (SELECT id FROM location WHERE code = 'A-MIR'));
INSERT INTO location (code, name, location_type_id, parent_id)
VALUES ('B-AGR', 'Agrabad Branch', (SELECT id FROM location_type WHERE code = 'BRANCH'), (SELECT id FROM location WHERE code = 'A-AGR'));

-- materialized paths, one level per statement (portable: no recursive UPDATE)
UPDATE location SET path = '/' || id || '/' WHERE parent_id IS NULL;
UPDATE location c SET path = (SELECT p.path FROM location p WHERE p.id = c.parent_id) || c.id || '/'
 WHERE c.path IS NULL AND c.parent_id IN (SELECT id FROM location WHERE path IS NOT NULL);
UPDATE location c SET path = (SELECT p.path FROM location p WHERE p.id = c.parent_id) || c.id || '/'
 WHERE c.path IS NULL AND c.parent_id IN (SELECT id FROM location WHERE path IS NOT NULL);
UPDATE location c SET path = (SELECT p.path FROM location p WHERE p.id = c.parent_id) || c.id || '/'
 WHERE c.path IS NULL AND c.parent_id IN (SELECT id FROM location WHERE path IS NOT NULL);

-- ---------- Org unit types ---------------------------------------------
INSERT INTO org_unit_type (code, name, level_no) VALUES ('ORGANIZATION', 'Organization', 1);
INSERT INTO org_unit_type (code, name, level_no) VALUES ('FUNCTION',     'Function',     2);
INSERT INTO org_unit_type (code, name, level_no) VALUES ('DEPARTMENT',   'Department',   3);
INSERT INTO org_unit_type (code, name, level_no) VALUES ('UNIT',         'Unit',         4);
INSERT INTO org_unit_type (code, name, level_no) VALUES ('TEAM',         'Team',         5);

-- ---------- Org units (organogram) -------------------------------------
INSERT INTO org_unit (code, name, org_unit_type_id, nature, parent_id, location_id, sort_order)
VALUES ('ORG', 'Sample Organization', (SELECT id FROM org_unit_type WHERE code = 'ORGANIZATION'), NULL, NULL,
        (SELECT id FROM location WHERE code = 'B-HO'), 1);

INSERT INTO org_unit (code, name, org_unit_type_id, nature, parent_id, location_id, sort_order)
VALUES ('F-TECH', 'Technology', (SELECT id FROM org_unit_type WHERE code = 'FUNCTION'), 'FUNCTIONAL',
        (SELECT id FROM org_unit WHERE code = 'ORG'), (SELECT id FROM location WHERE code = 'B-HO'), 1);
INSERT INTO org_unit (code, name, org_unit_type_id, nature, parent_id, location_id, sort_order)
VALUES ('F-CS', 'Corporate Services', (SELECT id FROM org_unit_type WHERE code = 'FUNCTION'), 'NON_FUNCTIONAL',
        (SELECT id FROM org_unit WHERE code = 'ORG'), (SELECT id FROM location WHERE code = 'B-HO'), 2);

INSERT INTO org_unit (code, name, org_unit_type_id, nature, parent_id, location_id, sort_order)
VALUES ('D-SE', 'Software Engineering', (SELECT id FROM org_unit_type WHERE code = 'DEPARTMENT'), 'FUNCTIONAL',
        (SELECT id FROM org_unit WHERE code = 'F-TECH'), (SELECT id FROM location WHERE code = 'B-HO'), 1);
INSERT INTO org_unit (code, name, org_unit_type_id, nature, parent_id, location_id, sort_order)
VALUES ('D-QA', 'Quality Assurance', (SELECT id FROM org_unit_type WHERE code = 'DEPARTMENT'), 'FUNCTIONAL',
        (SELECT id FROM org_unit WHERE code = 'F-TECH'), (SELECT id FROM location WHERE code = 'B-HO'), 2);
INSERT INTO org_unit (code, name, org_unit_type_id, nature, parent_id, location_id, sort_order)
VALUES ('D-HR', 'Human Resources', (SELECT id FROM org_unit_type WHERE code = 'DEPARTMENT'), 'NON_FUNCTIONAL',
        (SELECT id FROM org_unit WHERE code = 'F-CS'), (SELECT id FROM location WHERE code = 'B-HO'), 1);
INSERT INTO org_unit (code, name, org_unit_type_id, nature, parent_id, location_id, sort_order)
VALUES ('D-FIN', 'Finance and Accounts', (SELECT id FROM org_unit_type WHERE code = 'DEPARTMENT'), 'NON_FUNCTIONAL',
        (SELECT id FROM org_unit WHERE code = 'F-CS'), (SELECT id FROM location WHERE code = 'B-HO'), 2);

INSERT INTO org_unit (code, name, org_unit_type_id, nature, parent_id, location_id, sort_order)
VALUES ('U-ERP', 'ERP Unit', (SELECT id FROM org_unit_type WHERE code = 'UNIT'), 'FUNCTIONAL',
        (SELECT id FROM org_unit WHERE code = 'D-SE'), (SELECT id FROM location WHERE code = 'B-MIR10'), 1);

UPDATE org_unit SET path = '/' || id || '/' WHERE parent_id IS NULL;
UPDATE org_unit c SET path = (SELECT p.path FROM org_unit p WHERE p.id = c.parent_id) || c.id || '/'
 WHERE c.path IS NULL AND c.parent_id IN (SELECT id FROM org_unit WHERE path IS NOT NULL);
UPDATE org_unit c SET path = (SELECT p.path FROM org_unit p WHERE p.id = c.parent_id) || c.id || '/'
 WHERE c.path IS NULL AND c.parent_id IN (SELECT id FROM org_unit WHERE path IS NOT NULL);
UPDATE org_unit c SET path = (SELECT p.path FROM org_unit p WHERE p.id = c.parent_id) || c.id || '/'
 WHERE c.path IS NULL AND c.parent_id IN (SELECT id FROM org_unit WHERE path IS NOT NULL);

-- ---------- Designations (grade_rank 1 = most senior) -------------------
INSERT INTO designation (code, name, grade_rank) VALUES ('CEO', 'Chief Executive Officer', 1);
INSERT INTO designation (code, name, grade_rank) VALUES ('HOF', 'Head of Function',        2);
INSERT INTO designation (code, name, grade_rank) VALUES ('SM',  'Senior Manager',          3);
INSERT INTO designation (code, name, grade_rank) VALUES ('MGR', 'Manager',                 4);
INSERT INTO designation (code, name, grade_rank) VALUES ('SO',  'Senior Officer',          5);
INSERT INTO designation (code, name, grade_rank) VALUES ('OFF', 'Officer',                 6);

-- ---------- Sanctioned posts --------------------------------------------
INSERT INTO org_unit_post (org_unit_id, designation_id, sanctioned_count)
SELECT u.id, d.id, v.cnt
FROM (VALUES ('ORG', 'CEO', 1),
             ('F-TECH', 'HOF', 1),
             ('D-SE', 'SM', 1), ('D-SE', 'MGR', 2), ('D-SE', 'SO', 4),
             ('U-ERP', 'MGR', 1), ('U-ERP', 'SO', 3), ('U-ERP', 'OFF', 2),
             ('D-QA', 'MGR', 1), ('D-QA', 'SO', 2),
             ('F-CS', 'HOF', 1),
             ('D-HR', 'MGR', 1), ('D-HR', 'SO', 2),
             ('D-FIN', 'MGR', 1), ('D-FIN', 'OFF', 2)) AS v (unit_code, desig_code, cnt)
JOIN org_unit u ON u.code = v.unit_code
JOIN designation d ON d.code = v.desig_code;

-- ---------- Employees (inserted top-down so supervisors exist first) ----
INSERT INTO employee (employee_code, full_name, email, phone, join_date, designation_id, org_unit_id, location_id, supervisor_id)
VALUES ('E0001', 'Rahim Uddin', 'rahim.uddin@example.com', '+8801700000001', DATE '2015-01-01',
        (SELECT id FROM designation WHERE code = 'CEO'), (SELECT id FROM org_unit WHERE code = 'ORG'),
        (SELECT id FROM location WHERE code = 'B-HO'), NULL);
INSERT INTO employee (employee_code, full_name, email, phone, join_date, designation_id, org_unit_id, location_id, supervisor_id)
VALUES ('E0002', 'Karim Ahmed', 'karim.ahmed@example.com', '+8801700000002', DATE '2016-03-15',
        (SELECT id FROM designation WHERE code = 'HOF'), (SELECT id FROM org_unit WHERE code = 'F-TECH'),
        (SELECT id FROM location WHERE code = 'B-HO'), (SELECT id FROM employee WHERE employee_code = 'E0001'));
INSERT INTO employee (employee_code, full_name, email, phone, join_date, designation_id, org_unit_id, location_id, supervisor_id)
VALUES ('E0006', 'Sabbir Rahman', 'sabbir.rahman@example.com', '+8801700000006', DATE '2017-07-01',
        (SELECT id FROM designation WHERE code = 'HOF'), (SELECT id FROM org_unit WHERE code = 'F-CS'),
        (SELECT id FROM location WHERE code = 'B-HO'), (SELECT id FROM employee WHERE employee_code = 'E0001'));
INSERT INTO employee (employee_code, full_name, email, phone, join_date, designation_id, org_unit_id, location_id, supervisor_id)
VALUES ('E0003', 'Nusrat Jahan', 'nusrat.jahan@example.com', '+8801700000003', DATE '2018-02-01',
        (SELECT id FROM designation WHERE code = 'SM'), (SELECT id FROM org_unit WHERE code = 'D-SE'),
        (SELECT id FROM location WHERE code = 'B-HO'), (SELECT id FROM employee WHERE employee_code = 'E0002'));
INSERT INTO employee (employee_code, full_name, email, phone, join_date, designation_id, org_unit_id, location_id, supervisor_id)
VALUES ('E0007', 'Mehedi Hasan', 'mehedi.hasan@example.com', '+8801700000007', DATE '2019-05-12',
        (SELECT id FROM designation WHERE code = 'MGR'), (SELECT id FROM org_unit WHERE code = 'D-HR'),
        (SELECT id FROM location WHERE code = 'B-HO'), (SELECT id FROM employee WHERE employee_code = 'E0006'));
INSERT INTO employee (employee_code, full_name, email, phone, join_date, designation_id, org_unit_id, location_id, supervisor_id)
VALUES ('E0008', 'Sharmin Sultana', 'sharmin.sultana@example.com', '+8801700000008', DATE '2021-09-01',
        (SELECT id FROM designation WHERE code = 'OFF'), (SELECT id FROM org_unit WHERE code = 'D-FIN'),
        (SELECT id FROM location WHERE code = 'B-AGR'), (SELECT id FROM employee WHERE employee_code = 'E0006'));
INSERT INTO employee (employee_code, full_name, email, phone, join_date, designation_id, org_unit_id, location_id, supervisor_id)
VALUES ('E0004', 'Tanvir Hasan', 'tanvir.hasan@example.com', '+8801700000004', DATE '2019-11-03',
        (SELECT id FROM designation WHERE code = 'MGR'), (SELECT id FROM org_unit WHERE code = 'U-ERP'),
        (SELECT id FROM location WHERE code = 'B-MIR10'), (SELECT id FROM employee WHERE employee_code = 'E0003'));
INSERT INTO employee (employee_code, full_name, email, phone, join_date, designation_id, org_unit_id, location_id, supervisor_id)
VALUES ('E0005', 'Farhana Akter', 'farhana.akter@example.com', '+8801700000005', DATE '2022-01-10',
        (SELECT id FROM designation WHERE code = 'SO'), (SELECT id FROM org_unit WHERE code = 'U-ERP'),
        (SELECT id FROM location WHERE code = 'B-MIR10'), (SELECT id FROM employee WHERE employee_code = 'E0004'));
