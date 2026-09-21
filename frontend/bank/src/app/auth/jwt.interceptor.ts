import { HttpInterceptorFn } from '@angular/common/http';

export const jwtInterceptor: HttpInterceptorFn = (req, next) => {

  // ✅ Do NOT attach token for auth & public endpoints
  if (
    req.url.includes('/api/auth/') ||
    req.url.includes('/swagger') ||
    req.url.includes('/v3/api-docs')
  ) {
    return next(req);
  }

  const token = sessionStorage.getItem('token');

  if (token) {
    req = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });
  }

  return next(req);
};