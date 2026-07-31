import {HttpClient, HttpErrorResponse, HttpEvent, HttpHandlerFn, HttpInterceptorFn, HttpRequest} from '@angular/common/http';
import {inject} from '@angular/core';
import {Router} from '@angular/router';
import {SnackBarService} from '@core/services/snack-bar.service';
import {TranslateService} from '@ngx-translate/core';
import {Level} from '@shared/core/layout/notification-template/notification-template.component';
import {BehaviorSubject, EMPTY, Observable, of, throwError} from 'rxjs';
import {catchError, filter, map, switchMap, take, tap} from 'rxjs/operators';
import {AuthenticationService} from '../services/authentication.service';

/**
 * Code de retour HTTP : erreur métier
 */
const HTTP_CODE_BUSINESS_ERROR = 409;

/**
 * Temps d'affichage de la snackbar d'une business error
 */
const BUSINESS_ERROR_SNACKBAR_DURATION = 5000;

/**
 * Code de retour HTTP : token expiré
 */
const HTTP_CODE_TOKEN_EXPIRED = 498;

/**
 * Préfixe des clés de translate des erreurs métier
 */
const BUSINESS_ERROR_TRANSLATE_KEY_PREFIX = 'businessException';

/**
 * Wrapper sur le type <any>
 */
// tslint:disable-next-line:no-any : on utilise any car on intercepte TOUT type de requête
type HttpEventAny = HttpEvent<any>;

/**
 * Wrapper sur le type <any>
 */
// tslint:disable-next-line:no-any : on utilise any car on intercepte TOUT type de requête
type HttpRequestAny = HttpRequest<any>;


export const ResponseTokenInterceptor: HttpInterceptorFn = (request, next) => {
    // Injection des services
    const authenticationService = inject(AuthenticationService);
    const translateService = inject(TranslateService);
    const snackBarService = inject(SnackBarService);
    const http = inject(HttpClient);
    const router = inject(Router);

    // Variables d'état
    const refreshIsRunning = ResponseTokenInterceptor_refreshIsRunning();
    const REFRESH_TOKEN_URL = '/refresh_token';

    // Wrapper sur le type <any>
    type HttpEventAny = HttpEvent<any>;
    type HttpRequestAny = HttpRequest<any>;

    // Fonction pour injecter les headers
    function injectHeadersAndCloneRequest(request: HttpRequestAny): Observable<HttpEventAny> {
        return next(
            request.clone({
                setHeaders: authenticationService.getHeadersForRequestInjection(),
                withCredentials: true
            })
        );
    }

    // Fonction pour attendre la fin du refresh
    function waitForRefreshTokenOrDo(observable: Observable<unknown>): Observable<unknown> {
        if (refreshIsRunning.getValue() === true) {
            return refreshIsRunning.asObservable().pipe(
                filter((isRunning: boolean) => isRunning === false),
                take(1),
                switchMap(() => observable)
            );
        } else {
            return observable;
        }
    }

    // URL d'authentification anonyme
    const ANONYMOUS_URL = '/anonymous';

    // Fonction pour gérer l'erreur HTTP
    function handleHttpError(request: HttpRequestAny, error: HttpErrorResponse): Observable<HttpEventAny> {
        if (error.status === HTTP_CODE_BUSINESS_ERROR) {
            const apiError = error.error;
            const translateKey = BUSINESS_ERROR_TRANSLATE_KEY_PREFIX + '.' + apiError.code;
            let errorMessage = translateService.instant(translateKey);
            if (errorMessage == null || errorMessage === '' || errorMessage === translateKey) {
                errorMessage = apiError.label;
            }
            snackBarService.openSnackBar({
                message: errorMessage,
                level: Level.ERROR,
            }, BUSINESS_ERROR_SNACKBAR_DURATION);
            return of();
        } else if (error.status === HTTP_CODE_TOKEN_EXPIRED) {
            if (refreshIsRunning.getValue()) {
                return waitForRefreshTokenOrDo(of(request))
                    .pipe(
                        switchMap((requestDelayed: HttpRequestAny) => injectHeadersAndCloneRequest(requestDelayed))
                    );
            }
            else {
                AuthenticationService.clearAuhtenticatedTokenInSession();
                return refreshToken().pipe(
                    switchMap(() => injectHeadersAndCloneRequest(request)),
                    catchError(() => {
                        AuthenticationService.clearTokens();
                        router.navigate(['/login'], {
                            state: { snackBar: 'error.sessionExpired' }
                        });
                        return EMPTY;
                    })
                );
            }
        }
        return throwError(() => error);
    }

    // Fonction pour refresh le token
    function refreshToken(): Observable<HttpEventAny> {
        refreshIsRunning.next(true);
        return http.get(
            REFRESH_TOKEN_URL,
            {
                headers: authenticationService.getHeadersForRefreshToken(),
                observe: 'response',
                reportProgress: false,
                withCredentials: false
            }
        ).pipe(
            catchError((error) => {
                refreshIsRunning.next(false);
                return throwError(() => error);
            }),
            tap(() => refreshIsRunning.next(false))
        );
    }

    // Exécution principale
    const requestWithHeaders = injectHeadersAndCloneRequest(request);
    if (request.url.includes(REFRESH_TOKEN_URL)) {
        return requestWithHeaders;
    }
    return waitForRefreshTokenOrDo(requestWithHeaders).pipe(
        catchError((error: HttpErrorResponse) => handleHttpError(request, error)),
        map((project: HttpEventAny) => project)
    );
};

// Utilitaire pour stocker l'état du refresh (singleton par injection context)
function ResponseTokenInterceptor_refreshIsRunning() {
    // Angular inject() context is per request, so we use a static variable
    // to mimic the previous class property
    if (!(window as any)._ResponseTokenInterceptor_refreshIsRunning) {
        (window as any)._ResponseTokenInterceptor_refreshIsRunning = new BehaviorSubject<boolean>(false);
    }
    return (window as any)._ResponseTokenInterceptor_refreshIsRunning as BehaviorSubject<boolean>;
}
