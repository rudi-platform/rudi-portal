import {HttpClient, HttpErrorResponse, HttpEvent, HttpInterceptorFn, HttpRequest} from '@angular/common/http';
import {inject} from '@angular/core';
import {Router} from '@angular/router';
import {SnackBarService} from '@core/services/snack-bar.service';
import {TranslateService} from '@ngx-translate/core';
import {Level} from '@shared/core/layout/notification-template/notification-template.component';
import {BehaviorSubject, EMPTY, Observable, of, throwError} from 'rxjs';
import {catchError, filter, map, switchMap, take, tap} from 'rxjs/operators';
import {AuthenticationService} from '../services/authentication.service';
import {RouteProtectionService} from '../services/route-protection.service';

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
    /**
     * ========== RÔLE DE CET INTERCEPTOR ==========
     *
     * Cet interceptor est le point central de gestion de l'authentification JWT et des erreurs HTTP.
     *
     * Responsabilités principales :
     * 1. INJECTION DE TOKENS : Ajouter le token JWT à CHAQUE requête HTTP
     * 2. GESTION DES EXPIRATIONS : Rafraîchir le token automatiquement si expiré (HTTP 498)
     * 3. GESTION DES ERREURS : Traiter les erreurs métier (409), les sessions expirées (401), etc.
     * 4. REDIRECTION : Rediriger vers login si authentification complètement perdue
     *
     * Important : Le comportement varie selon le TYPE DE ROUTE :
     * - Routes PROTÉGÉES (/personal-space, /projets/soumettre-un-projet, etc.)
     *   → Déclenchent un refresh du token et une redirection si le refresh échoue
     * - Routes PUBLIQUES (/catalogue, /datasets, pages d'accueil, etc.)
     *   → Laissent l'erreur 498 se propager sans forcer de refresh
     *
     * Cette distinction permet aux utilisateurs anonymes de consulter les pages publiques
     * même si leur token d'accès a expiré, tout en forçant une reconnexion sur les pages protégées.
     */
    /**
     * ========== RÔLE DE CET INTERCEPTOR ==========
     *
     * Cet interceptor est le point central de gestion de l'authentification JWT et des erreurs HTTP.
     *
     * Responsabilités principales :
     * 1. INJECTION DE TOKENS : Ajouter le token JWT à CHAQUE requête HTTP
     * 2. GESTION DES EXPIRATIONS : Rafraîchir le token automatiquement si expiré (HTTP 498)
     * 3. GESTION DES ERREURS : Traiter les erreurs métier (409), les sessions expirées (401), etc.
     * 4. REDIRECTION : Rediriger vers login si authentification complètement perdue
     *
     * Important : Le comportement varie selon le TYPE DE ROUTE :
     * - Routes PROTÉGÉES (/personal-space, /projets/soumettre-un-projet, etc.)
     *   → Déclenchent un refresh du token et une redirection si le refresh échoue
     * - Routes PUBLIQUES (/catalogue, /datasets, pages d'accueil, etc.)
     *   → Laissent l'erreur 498 se propager sans forcer de refresh
     *
     * Cette distinction permet aux utilisateurs anonymes de consulter les pages publiques
     * même si leur token d'accès a expiré, tout en forçant une reconnexion sur les pages protégées.
     */
        // Injection des services
    const authenticationService = inject(AuthenticationService);
    const translateService = inject(TranslateService);
    const snackBarService = inject(SnackBarService);
    const http = inject(HttpClient);
    const router = inject(Router);
    const routeProtectionService = inject(RouteProtectionService);

    /**
     * Variables d'état et constantes
     */
        // State partagé : coordonne les refresh de tokens concurrents
    const refreshIsRunning = ResponseTokenInterceptor_refreshIsRunning();
    // Endpoint pour obtenir un nouveau token JWT via le refresh token
    const REFRESH_TOKEN_URL = '/refresh_token';
    // Préfixe pour identifier les requêtes d'authentification sans token
    const ANONYMOUS_URL = '/anonymous';

    // Wrapper sur le type <any>
    type HttpEventAny = HttpEvent<any>;
    type HttpRequestAny = HttpRequest<any>;

    /**
     * Clone la requête HTTP et y ajoute les headers d'authentification nécessaires.
     *
     * Headers injectés : token JWT, credentials, etc.
     *
     * @param request - La requête HTTP originale
     * @returns Observable de la réponse HTTP
     */
    function injectHeadersAndCloneRequest(request: HttpRequestAny): Observable<HttpEventAny> {
        return next(
            request.clone({
                setHeaders: authenticationService.getHeadersForRequestInjection(),
                withCredentials: true
            })
        );
    }

    /**
     * Attend que le refresh du token soit terminé avant d'exécuter l'observable fourni.
     *
     * Permet aux requêtes concurrentes d'attendre que le premier refresh se termine,
     * évitant ainsi les problèmes de race condition lors d'expirations multiples simultanées.
     *
     * Cas :
     * - Si un refresh est en cours : attendre qu'il se termine, puis exécuter l'observable
     * - Sinon : exécuter immédiatement l'observable
     *
     * @param observable - L'observable à exécuter après le refresh (ou immédiatement)
     * @returns Observable qui s'exécutera après le refresh
     */
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

    /**
     * Gère les erreurs HTTP retournées par les appels backend.
     * Implémente une logique multi-niveaux basée sur le type d'erreur et le contexte de la route courante.
     *
     * Cas traités :
     * 1. Erreur 401 : Authentification invalide (session utilisateur expirée côté serveur)
     * 2. Erreur 409 : Erreur métier (violation de règle métier)
     * 3. Erreur 498 : Token JWT expiré (deux sous-cas selon l'état du refresh)
     */
    function handleHttpError(request: HttpRequestAny, error: HttpErrorResponse): Observable<HttpEventAny> {
        const isAuthenticatedAsUser = authenticationService.isAuthenticatedAsUser();

        /**
         * CAS 1 : Erreur 401 - Session utilisateur expirée côté serveur
         *
         * Conditions :
         * - error.status === 401
         * - La requête n'est pas une requête anonyme
         *
         * Comportement :
         * - Si l'utilisateur était authentifié : effacer les tokens et rediriger vers login avec message
         * - Sinon : propager l'erreur (cas rare)
         */
        if (error.status === 401 && !request.url.includes(ANONYMOUS_URL)) {
            const isAuth = authenticationService.isAuthenticatedAsUser();
            if (isAuth) {
                AuthenticationService.clearTokens();
                router.navigate(['/login'], {
                    state: {snackBar: 'error.sessionExpired'}
                });
                return EMPTY;
            }
            return throwError(() => error);
        }

        /**
         * CAS 2 : Erreur 409 - Erreur métier (contrainte métier violée)
         *
         * Conditions :
         * - error.status === HTTP_CODE_BUSINESS_ERROR (409)
         *
         * Comportement :
         * - Afficher le message d'erreur métier via snackbar
         * - Continuer l'exécution (retourner un observable vide)
         * - L'erreur est traitée sans redirection, utilisateur reste sur la même page
         */
        else if (error.status === HTTP_CODE_BUSINESS_ERROR) {
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
        }

        /**
         * CAS 3A : Erreur 498 - Token JWT expiré (PREMIER appel durant une expiration)
         *
         * Conditions :
         * - error.status === HTTP_CODE_TOKEN_EXPIRED (498)
         * - La requête n'est pas une requête anonyme
         * - La route courante est protégée (nécessite authentification)
         * - Pas d'autre refresh en cours (!refreshIsRunning)
         *
         * Comportement :
         * - Afficher une notification "token expiré" à l'utilisateur
         * - Effacer SEULEMENT le token JWT de la session (pas l'état d'authentification)
         * - Déclencher un refresh du token
         * - Si le refresh réussit : relancer la requête originale avec le nouveau token
         * - Si le refresh échoue : nettoyer complètement et rediriger vers login
         *
         * Raison de cette logique :
         * - Sur une route protégée, l'utilisateur doit être reconnecté si le token ne peut être rafraîchi
         * - L'alerte avertit l'utilisateur que son authentification a expiré
         */
        else if (error.status === HTTP_CODE_TOKEN_EXPIRED
            && !request.url.includes(ANONYMOUS_URL)
            && routeProtectionService.isCurrentRouteProtected()
            && !refreshIsRunning.getValue()) {
            AuthenticationService.clearAuhtenticatedTokenInSession();
            return refreshToken().pipe(
                switchMap(() => injectHeadersAndCloneRequest(request)),
                catchError(() => {
                    AuthenticationService.clearTokens();
                    router.navigate(['/login'], {
                        state: {snackBar: 'error.sessionExpired'}
                    });
                    return EMPTY;
                })
            );
        }

        /**
         * CAS 3B : Erreur 498 - Token JWT expiré (AUTRES appels PENDANT un refresh en cours)
         *
         * Conditions
         * - Si error.status === HTTP_CODE_TOKEN_EXPIRED (498).
         * - La requête n'est pas une requête anonyme.
         * - La route courante est protégée (nécessite authentification).
         * - Un refresh est DÉJÀ en cours (refreshIsRunning === true).
         *
         * Comportement
         * - Attendre que le refresh en cours se termine
         * - Relancer la requête originale avec le nouveau token
         * - Évite les requêtes de refresh multiples et concurrentes
         *
         * Raison de cette logique
         * - Si plusieurs requêtes expirent simultanément, une seule devrait déclencher le refresh
         * - Les autres attendent que ce premier refresh se termine, puis utilisent le nouveau token
         * - Cela évite de surcharger le serveur avec plusieurs refresh identiques
         */
        else if (error.status === HTTP_CODE_TOKEN_EXPIRED
            && !request.url.includes(ANONYMOUS_URL)
            && routeProtectionService.isCurrentRouteProtected()
            && refreshIsRunning.getValue()) {
            return waitForRefreshTokenOrDo(of(request))
                .pipe(
                    switchMap((requestDelayed: HttpRequestAny) => injectHeadersAndCloneRequest(requestDelayed))
                );
        }

        /**
         * CAS PAR DÉFAUT : Autres erreurs
         *
         * Comportement :
         * - Propager l'erreur sans traitement spécial
         * - Permet au service appelant de gérer l'erreur selon son contexte
         */
        return throwError(() => error);
    }

    /**
     * Rafraîchit le token JWT auprès du serveur.
     *
     * Processus :
     * 1. Marquer que le refresh est en cours (refreshIsRunning = true)
     * 2. Appeler l'endpoint /refresh_token avec le refresh token
     * 3. Enregistrer le nouveau token JWT retourné
     * 4. Marquer le refresh comme terminé (refreshIsRunning = false)
     * 5. En cas d'erreur, également marquer le refresh comme terminé
     *
     * Les autres requêtes concurrentes peuvent attendre la fin de ce refresh
     * avant de relancer leurs propres requêtes (voir waitForRefreshTokenOrDo).
     *
     * @returns Observable de la réponse du refresh (contient le nouveau token)
     */
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

    /**
     * ========== LOGIQUE PRINCIPALE DE L'INTERCEPTOR ==========
     *
     * Flux de traitement :
     * 1. Injecter les headers d'authentification dans TOUTES les requêtes
     * 2. Si c'est une requête vers /refresh_token : la laisser passer sans traitement supplémentaire
     * 3. Sinon : attendre que le refresh en cours soit terminé (si applicable), puis envoyer la requête
     * 4. Capturer et traiter les erreurs HTTP avec handleHttpError
     *
     * Points clés
     * - Les requêtes d'authentification anonyme ne sont pas interceptées pour les redirections
     * - Les routes non protégées ne déclenchent pas de refresh sur token expiré
     * - Les erreurs HTTP 498 peuvent déclencher un refresh automatique sur routes protégées
     * - Les erreurs 401 et 409 sont gérées de manière spécifique
     */
    const requestWithHeaders = injectHeadersAndCloneRequest(request);
    if (request.url.includes(REFRESH_TOKEN_URL)) {
        return requestWithHeaders;
    }
    return waitForRefreshTokenOrDo(requestWithHeaders).pipe(
        catchError((error: HttpErrorResponse) => handleHttpError(request, error)),
        map((project: HttpEventAny) => project)
    );
};

/**
 * ========== UTILITAIRE SINGLETON POUR ÉTAT DU REFRESH ==========
 *
 * Stocke l'état global du refresh du token pour coordonner les requêtes concurrentes.
 *
 * Problème résolu :
 * - Si 5 requêtes expirent simultanément, sans coordination, chacune déclencherait
 *   un appel /refresh_token → 5 appels identiques au serveur
 *
 * Solution implémentée :
 * - Un BehaviorSubject global (via window) stocke si un refresh est en cours
 * - Le PREMIER appel avec HTTP 498 déclenche le refresh et met refreshIsRunning = true
 * - Les 4 autres attendent que le refresh se termine (refreshIsRunning devient false)
 * - Puis elles utilisent le nouveau token sans déclencher de nouveau refresh
 *
 * Stockage :
 * - Utilise window._ResponseTokenInterceptor_refreshIsRunning comme singleton
 * - Persiste pour toute la durée de la session utilisateur
 * - Partagé entre tous les appels HTTP de la même application
 *
 * @returns BehaviorSubject<boolean> - true si refresh en cours, false sinon
 */
function ResponseTokenInterceptor_refreshIsRunning() {
    // Angular inject() context is per request, so we use a static variable
    // to mimic the previous class property
    if (!(window as any)._ResponseTokenInterceptor_refreshIsRunning) {
        (window as any)._ResponseTokenInterceptor_refreshIsRunning = new BehaviorSubject<boolean>(false);
    }
    return (window as any)._ResponseTokenInterceptor_refreshIsRunning as BehaviorSubject<boolean>;
}
