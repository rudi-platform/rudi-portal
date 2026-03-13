import {Injectable} from '@angular/core';
import {ActivatedRouteSnapshot, Router, RouterStateSnapshot, UrlTree} from '@angular/router';
import {AuthenticationService} from '@core/services/authentication.service';
import {LogService} from '@core/services/log.service';
import {UserService} from '@core/services/user.service';
import {Observable, of} from 'rxjs';
import {catchError, map, switchMap, take} from 'rxjs/operators';
import { AccountService } from './account.service';

@Injectable({
    providedIn: 'root'
})
export class AuthGuardService {

    // Gestion d'un loader durant l'authentification
    public loader = false;

    constructor(
        public readonly authenticationService: AuthenticationService,
        private readonly userService: UserService,
        private readonly accountService: AccountService,
        private readonly router: Router,
        private readonly logService: LogService) {
    }

    /**
     * tentative d'authent auto en mode anonymous
     * @return Observable<boolean> un observable qui dit si on a réussit ou non
     */
    private authenticateAnonymousAutomatically(): Observable<boolean> {
        // je tente l'authent
        return this.authenticationService.authenticateAsAnonymous().pipe(
            // C'est ok : je passe
            switchMap(() => of(true)),
            // C'est ko : je me redirige vers la page unauthorized (erreur grave)
            catchError(error => {
                this.logService.error('Authentication error', error);
                return this.redirectToNotAuthorized();
            })
        );
    }

    /**
     * Redirection vers unauthorized (la route)
     * @private
     */
    private redirectToNotAuthorized(): Observable<boolean> {
        // Le routeur fais go unauthorized
        this.router.navigate(['/not-authorized']);
        // je renvoie faux car c'est pas bon
        return of(false);
    }

    /**
     * Accès aux urls de même niveau
     * Renvoie true si utilisateur a le droit et false pas le droit
     * @returns Observable<any>
     */
    canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot):
        Observable<boolean | UrlTree> | Promise<boolean | UrlTree> | boolean | UrlTree {

        if (route.queryParamMap.has("token") && route.url[0].path === "home") {
            // pas de token en localstorage, mais on l'a dans l'url
            const token = route.queryParamMap.get("token") ?? "";
            return this.accountService.authenticate(token).pipe(
                take(1),
                map(() => {
                    this.authenticationService.authnenticateAsCas();
                    return this.router.parseUrl(this.authenticationService.getRoute());
                }),
            );
        }

        return this.userService.getAuthenticatedUser().pipe(
            // ça marche : je peux accéder normalement à la page en restant authentifié
            map(() => true),
            // KO : j'ai pas accès à la page, j'ai perdu mon authent donc j'essaye de m'authent en anonymous
            catchError(() => this.authenticateAnonymousAutomatically())
        );
    }

    /**
     * Accès aux urls enfants même traitement que les url parents
     * @returns Observable<any>
     */
    canActivateChild(route: ActivatedRouteSnapshot, state: RouterStateSnapshot):
        Observable<boolean | UrlTree> |
        Promise<boolean | UrlTree> | boolean | UrlTree {
        return this.canActivate(route, state);
    }
}
