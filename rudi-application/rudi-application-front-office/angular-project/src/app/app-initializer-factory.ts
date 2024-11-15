import {LOCATION_INITIALIZED} from '@angular/common';
import {Injector} from '@angular/core';
import {PropertiesMetierService} from '@core/services/properties-metier.service';
import {CustomTranslateService} from '@core/services/translate.service';
import {forkJoin, Observable, ReplaySubject} from 'rxjs';

const TRANSLATE_SERVICE_IS_READY = new ReplaySubject<void>();
/**
 * Déclenché au moment où le service de traduction est initialisé et peut être utilisé
 */
export const TRANSLATE_SERVICE_IS_READY$: Observable<void> = TRANSLATE_SERVICE_IS_READY.asObservable();

export function appInitializerFactory(translate: CustomTranslateService, injector: Injector, propertiesMetierService: PropertiesMetierService): () => Promise<void> {
    return () => new Promise<void>(resolve => {
        const locationInitialized = injector.get(LOCATION_INITIALIZED, Promise.resolve(null));
        forkJoin({
            locationInitialized,
            teamName: propertiesMetierService.get('front.teamName'),
            projectName: propertiesMetierService.get('front.projectName')
        }).subscribe(res => {
            const langToSet = 'fr';
            translate.setDefaultLang('fr');
            translate.setProjectName(res.projectName);
            translate.setTeamName(res.teamName);
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
