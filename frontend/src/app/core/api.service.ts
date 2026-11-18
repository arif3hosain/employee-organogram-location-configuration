import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, map, tap } from 'rxjs';
import {
  DesignationForm, DesignationView, EmployeeForm, GradeForm, GradeView, EmployeeSearch, EmployeeView,
  LocationForm, LocationNode, LocationView, OrgUnitForm, OrgUnitNature, OrgUnitNode,
  OrgUnitPostView, OrgUnitView, PageView, Result, TypeForm, TypeView,
} from './models';
import { NotifyService } from './notify.service';

/**
 * One typed client for the POC backend. Reads return the unwrapped `data`;
 * writes also show the backend's success message. Errors are shown by errorInterceptor.
 */
@Injectable({ providedIn: 'root' })
export class ApiService {
  private readonly http = inject(HttpClient);
  private readonly notify = inject(NotifyService);

  /* ---------------- location types ---------------- */
  locationTypes(): Observable<TypeView[]> { return this.get('/api/location-types'); }
  createLocationType(f: TypeForm): Observable<TypeView> { return this.send('POST', '/api/location-types', f); }
  updateLocationType(id: number, f: TypeForm): Observable<TypeView> { return this.send('PUT', `/api/location-types/${id}`, f); }
  deleteLocationType(id: number): Observable<number> { return this.send('DELETE', `/api/location-types/${id}`); }

  /* ---------------- locations ---------------- */
  locations(filter: { typeId?: number | null; parentId?: number | null; q?: string | null } = {}): Observable<LocationView[]> {
    return this.get('/api/locations', filter);
  }
  locationTree(rootId?: number | null): Observable<LocationNode[]> { return this.get('/api/locations/tree', { rootId }); }
  location(id: number): Observable<LocationView> { return this.get(`/api/locations/${id}`); }
  createLocation(f: LocationForm): Observable<LocationView> { return this.send('POST', '/api/locations', f); }
  updateLocation(id: number, f: LocationForm): Observable<LocationView> { return this.send('PUT', `/api/locations/${id}`, f); }
  deleteLocation(id: number): Observable<number> { return this.send('DELETE', `/api/locations/${id}`); }

  /* ---------------- org unit types ---------------- */
  orgUnitTypes(): Observable<TypeView[]> { return this.get('/api/org-unit-types'); }
  createOrgUnitType(f: TypeForm): Observable<TypeView> { return this.send('POST', '/api/org-unit-types', f); }
  updateOrgUnitType(id: number, f: TypeForm): Observable<TypeView> { return this.send('PUT', `/api/org-unit-types/${id}`, f); }
  deleteOrgUnitType(id: number): Observable<number> { return this.send('DELETE', `/api/org-unit-types/${id}`); }

  /* ---------------- grades ---------------- */
  grades(): Observable<GradeView[]> { return this.get('/api/grades'); }
  createGrade(f: GradeForm): Observable<GradeView> { return this.send('POST', '/api/grades', f); }
  updateGrade(id: number, f: GradeForm): Observable<GradeView> { return this.send('PUT', `/api/grades/${id}`, f); }
  deleteGrade(id: number): Observable<number> { return this.send('DELETE', `/api/grades/${id}`); }

  /* ---------------- designations ---------------- */
  designations(): Observable<DesignationView[]> { return this.get('/api/designations'); }
  createDesignation(f: DesignationForm): Observable<DesignationView> { return this.send('POST', '/api/designations', f); }
  updateDesignation(id: number, f: DesignationForm): Observable<DesignationView> { return this.send('PUT', `/api/designations/${id}`, f); }
  deleteDesignation(id: number): Observable<number> { return this.send('DELETE', `/api/designations/${id}`); }

  /* ---------------- org units (organogram) ---------------- */
  orgUnits(filter: { typeId?: number | null; parentId?: number | null; nature?: OrgUnitNature | null; q?: string | null } = {}): Observable<OrgUnitView[]> {
    return this.get('/api/org-units', filter);
  }
  orgUnitTree(rootId?: number | null): Observable<OrgUnitNode[]> { return this.get('/api/org-units/tree', { rootId }); }
  orgUnit(id: number): Observable<OrgUnitView> { return this.get(`/api/org-units/${id}`); }
  createOrgUnit(f: OrgUnitForm): Observable<OrgUnitView> { return this.send('POST', '/api/org-units', f); }
  updateOrgUnit(id: number, f: OrgUnitForm): Observable<OrgUnitView> { return this.send('PUT', `/api/org-units/${id}`, f); }
  deleteOrgUnit(id: number): Observable<number> { return this.send('DELETE', `/api/org-units/${id}`); }

  posts(orgUnitId: number): Observable<OrgUnitPostView[]> { return this.get(`/api/org-units/${orgUnitId}/posts`); }
  savePost(orgUnitId: number, designationId: number, sanctionedCount: number): Observable<OrgUnitPostView> {
    return this.send('PUT', `/api/org-units/${orgUnitId}/posts`, { designationId, sanctionedCount });
  }
  deletePost(postId: number): Observable<number> { return this.send('DELETE', `/api/org-units/posts/${postId}`); }

  /* ---------------- employees ---------------- */
  employees(s: EmployeeSearch): Observable<PageView<EmployeeView>> { return this.get('/api/employees', { ...s }); }
  employee(id: number): Observable<EmployeeView> { return this.get(`/api/employees/${id}`); }
  createEmployee(f: EmployeeForm): Observable<EmployeeView> { return this.send('POST', '/api/employees', f); }
  updateEmployee(id: number, f: EmployeeForm): Observable<EmployeeView> { return this.send('PUT', `/api/employees/${id}`, f); }
  deleteEmployee(id: number): Observable<EmployeeView> { return this.send('DELETE', `/api/employees/${id}`); }

  /* ---------------- plumbing ---------------- */

  private get<T>(url: string, query: Record<string, unknown> = {}): Observable<T> {
    return this.http.get<Result<T>>(url, { params: toParams(query) }).pipe(map(r => r.data));
  }

  private send<T>(method: 'POST' | 'PUT' | 'DELETE', url: string, body?: unknown): Observable<T> {
    return this.http.request<Result<T>>(method, url, { body }).pipe(
      tap(r => { if (r.message) this.notify.ok(r.message); }),
      map(r => r.data),
    );
  }
}

/** Drops null / undefined / empty values so the backend sees them as "no filter". */
function toParams(query: Record<string, unknown>): HttpParams {
  let params = new HttpParams();
  for (const [key, value] of Object.entries(query)) {
    if (value !== null && value !== undefined && value !== '') {
      params = params.set(key, String(value));
    }
  }
  return params;
}
