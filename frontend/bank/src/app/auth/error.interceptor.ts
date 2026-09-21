import { HttpInterceptorFn } from '@angular/common/http';
import { catchError, throwError } from 'rxjs';

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  return next(req).pipe(
    catchError((error) => {
      let message = 'An unexpected error occurred';

      if (error.status === 401) {
        message = 'Session expired. Please login again.';
      } else if (error.status === 403) {
        message = 'Access denied.';
      } else if (error.status === 400) {
        message = error.error || 'Invalid request.';
      } else if (error.status === 409) {
        message = error.error || 'Conflict: duplicate resource.';
      } else if (error.status === 422) {
        message = error.error || 'Unprocessable request.';
      } else if (error.status >= 500) {
        message = 'Server error. Please try again later.';
      }

      // Show notification (using alert for simplicity - can replace with MatSnackBar)
      if (typeof message === 'string' && error.status !== 401) {
        // Avoid duplicate alerts - only show for non-auth errors
        console.error(`[${error.status}] ${message}`);
      }

      return throwError(() => error);
    })
  );
};
