import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const router = inject(Router);
  const token = localStorage.getItem('token');

  if (token) req = req.clone({ setHeaders: { Authorization: `Bearer ${token}` } });
  
  return next(req).pipe(
    catchError((erro: HttpErrorResponse) => {
      if (erro.status === 401 && !req.url.includes('/auth/')) {
        localStorage.removeItem('token');
        localStorage.removeItem('nome');
        router.navigate(['/login']);
      }

      return throwError(() => erro);
    }),
  );
};
