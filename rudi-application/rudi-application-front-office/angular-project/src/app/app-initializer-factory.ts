import {LOCATION_INITIALIZED} from '@angular/common';
import {Injector} from '@angular/core';
import {TranslateService} from '@ngx-translate/core';
import {Observable, ReplaySubject} from 'rxjs';

const TRANSLATE_SERVICE_IS_READY = new ReplaySubject<void>();
/**
 * Déclenché au moment où le service de traduction est initialisé et peut être utilisé
 */
export const TRANSLATE_SERVICE_IS_READY$: Observable<void> = TRANSLATE_SERVICE_IS_READY.asObservable();

export function appInitializerFactory(translate: TranslateService, injector: Injector): () => Promise<void> {
    return () => new Promise<void>(resolve => {
        const locationInitialized = injector.get(LOCATION_INITIALIZED, Promise.resolve(null));
        locationInitialized.then(() => {
            const langToSet = 'fr';
            translate.setDefaultLang('fr');
            translate.use(langToSet).subscribe({
                next: () => TRANSLATE_SERVICE_IS_READY.next(void 0),
                error: (err) => {
                    console.error(`Problem with '${langToSet}' language initialization.'`);
                    TRANSLATE_SERVICE_IS_READY.error(err);
                },
                complete: () => {
                    resolve(null);
                    TRANSLATE_SERVICE_IS_READY.complete();
                }
            });
        });
    });
}
