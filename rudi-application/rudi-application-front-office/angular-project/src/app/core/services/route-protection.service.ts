import {Injectable} from '@angular/core';
import {Router} from '@angular/router';

/**
 * Service pour déterminer si une route donnée nécessite une authentification utilisateur réelle
 * (c'est-à-dire, n'accepte pas l'accès anonymous).
 */
@Injectable({
    providedIn: 'root'
})
export class RouteProtectionService {

    /**
     * Patterns de routes qui nécessitent une authentification utilisateur réelle (UserGuard)
     * Toutes les autres routes acceptent l'accès anonymous
     */
    private readonly protectedRoutePatterns = [
        '/personal-space',
        '/projets/soumettre-un-projet',
        '/catalogue/.*/selfdata-information-request-creation'
    ];

    constructor(private readonly router: Router) {
    }

    /**
     * Vérifie si la route actuelle nécessite une authentification utilisateur réelle
     * @returns true si la route est protégée (nécessite USER), false si elle accepte anonymous
     */
    isCurrentRouteProtected(): boolean {
        return this.isRouteProtected(this.router.url);
    }

    /**
     * Vérifie si une route donnée nécessite une authentification utilisateur réelle
     * @param url URL ou chemin à vérifier
     * @returns true si la route est protégée (nécessite USER), false si elle accepte anonymous
     */
    isRouteProtected(url: string): boolean {
        return this.protectedRoutePatterns.some(pattern => {
            const regex = new RegExp(pattern);
            return regex.test(url);
        });
    }
}

