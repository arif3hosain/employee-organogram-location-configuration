// Mirrors the backend DTOs (com.bracits.pms.*.api). Keep in sync with the Java records.

export type RecordStatus = 'ACTIVE' | 'INACTIVE';
export type OrgUnitNature = 'FUNCTIONAL' | 'NON_FUNCTIONAL';

/** Every endpoint returns this envelope (shared.action.Result). */
export interface Result<T> {
  success: boolean;
  message: string | null;
  data: T;
}

export interface PageView<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

/* ---------- typed setup lists (location types, org unit types, designations) ---------- */

export interface TypeView {
  id: number;
  code: string;
  name: string;
  levelNo: number;
  status: RecordStatus;
  version: number;
}

export interface TypeForm {
  code: string;
  name: string;
  levelNo: number;
  status: RecordStatus;
  version?: number | null;
}

export interface GradeView {
  id: number;
  code: string;
  name: string;
  rankNo: number;          // 1 = most senior
  status: RecordStatus;
  version: number;
}

export interface GradeForm {
  code: string;
  name: string;
  rankNo: number;
  status: RecordStatus;
  version?: number | null;
}

export interface DesignationView {
  id: number;
  code: string;
  name: string;
  gradeId: number;
  gradeCode: string;
  gradeName: string;
  gradeRank: number;       // the grade's rankNo, for display and sorting
  status: RecordStatus;
  version: number;
}

export interface DesignationForm {
  code: string;
  name: string;
  gradeId: number;
  status: RecordStatus;
  version?: number | null;
}

/* ---------- location ---------- */

export interface LocationView {
  id: number;
  code: string;
  name: string;
  typeId: number;
  typeCode: string;
  typeName: string;
  levelNo: number;
  parentId: number | null;
  parentName: string | null;
  path: string;
  status: RecordStatus;
  version: number;
}

export interface LocationNode {
  id: number;
  code: string;
  name: string;
  typeCode: string;
  typeName: string;
  levelNo: number;
  status: RecordStatus;
  children: LocationNode[];
}

export interface LocationForm {
  code: string;
  name: string;
  typeId: number | null;
  parentId: number | null;
  status: RecordStatus;
  version?: number | null;
}

/* ---------- organogram ---------- */

export interface OrgUnitView {
  id: number;
  code: string;
  name: string;
  typeId: number;
  typeCode: string;
  typeName: string;
  levelNo: number;
  nature: OrgUnitNature | null;
  parentId: number | null;
  parentName: string | null;
  locationId: number | null;
  locationName: string | null;
  sortOrder: number | null;
  path: string;
  status: RecordStatus;
  version: number;
}

export interface OrgUnitNode {
  id: number;
  code: string;
  name: string;
  typeCode: string;
  typeName: string;
  levelNo: number;
  nature: OrgUnitNature | null;
  locationName: string | null;
  status: RecordStatus;
  children: OrgUnitNode[];
}

export interface OrgUnitForm {
  code: string;
  name: string;
  typeId: number | null;
  nature: OrgUnitNature | null;
  parentId: number | null;
  locationId: number | null;
  sortOrder: number | null;
  status: RecordStatus;
  version?: number | null;
}

export interface OrgUnitPostView {
  id: number;
  orgUnitId: number;
  designationId: number;
  designationCode: string;
  designationName: string;
  gradeCode: string;
  gradeRank: number;
  sanctioned: number;
  filled: number;
  vacancy: number;
}

/* ---------- employee ---------- */

export interface EmployeeView {
  id: number;
  employeeCode: string;
  fullName: string;
  email: string;
  phone: string | null;
  joinDate: string;
  designationId: number;
  designationName: string;
  gradeId: number;
  gradeCode: string;
  gradeName: string;
  orgUnitId: number;
  orgUnitName: string;
  locationId: number;
  locationName: string;
  supervisorId: number | null;
  supervisorName: string | null;
  status: RecordStatus;
  version: number;
}

export interface EmployeeForm {
  employeeCode: string;
  fullName: string;
  email: string;
  phone: string | null;
  joinDate: string;
  designationId: number | null;
  orgUnitId: number | null;
  locationId: number | null;
  supervisorId: number | null;
  status: RecordStatus;
  version?: number | null;
}

export interface EmployeeSearch {
  q?: string | null;
  orgUnitId?: number | null;
  locationId?: number | null;
  designationId?: number | null;
  status?: RecordStatus | null;
  page: number;
  size: number;
}
