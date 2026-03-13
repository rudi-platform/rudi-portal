import { HttpEvent, HttpHandlerFn, HttpInterceptorFn, HttpRequest, HttpResponse } from '@angular/common/http';
import {inject, Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {tap} from 'rxjs/operators';
import {AuthenticationService} from '../services/authentication.service';

/**
 * Ajoute les informations d'authentification dans toutes les requêtes clientes
 */
export const HttpTokenInterceptor: HttpInterceptorFn = (request: HttpRequest<any>, next: HttpHandlerFn): Observable<HttpEvent<any>> => {
  const authenticationService = inject(AuthenticationService);

  return next(request).pipe(
    tap((httpResponse: HttpEvent<any>) => {
      if (httpResponse instanceof HttpResponse) {
        authenticationService.storeTokensFrom(httpResponse.headers);
      }
    })
  );
};
