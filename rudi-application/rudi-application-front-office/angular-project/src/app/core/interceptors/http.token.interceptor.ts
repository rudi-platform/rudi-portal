import { HttpEvent, HttpHandler, HttpInterceptor, HttpRequest, HttpResponse } from '@angular/common/http';
import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {tap} from 'rxjs/operators';
import {AuthenticationService} from '../services/authentication.service';

/**
 * Ajoute les informations d'authentification dans toutes les requêtes clientes
 */
@Injectable()
export class HttpTokenInterceptor implements HttpInterceptor {

    constructor(
        private readonly authentificationService: AuthenticationService,
    ) {
    }

    /**
     * Récupération du token rénouvelé dans le header de la reponse.
     */
    intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
        return this.execRequest(request, next);
    }

    private execRequest(request: HttpRequest<any>, next: HttpHandler) {
        return next.handle(request).pipe(
            tap((httpResponse: HttpEvent<any>) => {
                if (httpResponse instanceof HttpResponse) {
                    this.authentificationService.storeTokensFrom(httpResponse.headers);
                }
            })
        );
    }
}
