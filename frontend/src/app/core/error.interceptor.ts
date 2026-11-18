import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { NotifyService } from './notify.service';

/**
 * Failed preCondition (400), business rule in the service (409), not found (404):
 * the backend always sends { success:false, message }, so show that message.
 */
export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const notify = inject(NotifyService);
  return next(req).pipe(
    catchError((err: HttpErrorResponse) => {
      const message =
        err.error?.message ??
        (err.status === 0 || err.status === 504
          ? 'Backend not reachable. Is the Spring Boot app running on port 8080?'
          : `${err.status} ${err.statusText}`);
      notify.error(message);
      return throwError(() => err);
    }),
  );
};
