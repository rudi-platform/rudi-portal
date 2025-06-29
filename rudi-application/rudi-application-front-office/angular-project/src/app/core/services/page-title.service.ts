import {Injectable} from '@angular/core';
import {Meta, Title} from '@angular/platform-browser';
import {LogService} from '@core/services/log.service';
import {TranslateService} from '@ngx-translate/core';
import {GetBackendPropertyPipe} from '@shared/pipes/get-backend-property.pipe';
import {Observable} from 'rxjs';
import {defaultIfEmpty, filter, map, switchMap, take} from 'rxjs/operators';

const DEFAULT_PAGE_TITLE = 'Rudi';

@Injectable({
    providedIn: 'root'
})
export class PageTitleService {
    projectName: string;

    constructor(
        private readonly angularTitleService: Title,
        private readonly translateService: TranslateService,
        private readonly getBackendProperty: GetBackendPropertyPipe,
        private readonly logger: LogService,
        private readonly meta: Meta,
    ) {
    }

    /**
     * @param titles Tous les titres possibles, le premier non "falsy" est utilisé
     */
    public setPageTitle(...titles: string[]): void {
        this.getBackendProperty.transform('front.projectName').subscribe({
            next: projectName => {
                this.projectName = projectName;
                for (const title of titles) {
                    if (title) {
                        const actualTitle = title + ' - ' + this.projectName;
                        this.meta.updateTag({name: 'title', content: actualTitle});
                        this.angularTitleService.setTitle(actualTitle);

                        this.meta.updateTag({name: 'og:site_name', content: this.projectName});
                        return;
                    }
                }
            },
            error: err => {
                this.logger.error(err);
                this.projectName = DEFAULT_PAGE_TITLE;
                this.setDefaultPageTitle();
            }
        });
    }

    private setDefaultPageTitle(): void {
        this.angularTitleService.setTitle(this.projectName);
    }

    /**
     * @param fullUrl URL provenant du router Angular (commence par un "/" mais ne finit pas par un "/")
     * Modifie le nom de l'onglet en fonction de la route
     *  - L'algo fait la correspondance entre la route et une entrée du fichier de translate
     *  - Si aucune traduction ne correspond à la route, on remonte la route dossier par dossier jusqu'au premier ayant une traduction
     *  - Soit le contenu du fichier de traduction:
     *          "/projets": "Réutilisations",
     *          "/projets/soumettre-un-projet": "Projet",
     *          Si la route "/projets/declarer-une-reutilisation" n'a pas de traduction, alors la traduction pour la route
     *          "/projets" est prise
     */
    public setPageTitleFromUrl(fullUrl: string): void {

        /** Toutes les URL possibles en retirant à chaque fois le dernier dossier */
        const url$: Observable<string> = new Observable(subscriber => {
            let url = fullUrl;
            while (url && !subscriber.closed) {
                subscriber.next(url);

                const lastIndexOfSlash = url.lastIndexOf('/');
                if (lastIndexOfSlash === -1) {
                    break;
                }
                url = url.substring(0, lastIndexOfSlash);
            }
            subscriber.complete();
        });

        url$
            .pipe(
                switchMap(url => this.getPageTitleFromI18nOrNull(url)),
                filter(pageTitle => !!pageTitle),
                take(1),
                defaultIfEmpty(null)
            )
            .subscribe(pageTitle => {
                this.setPageTitle(pageTitle);
            });
    }

    private getPageTitleFromI18nOrNull(url: string): Observable<string | null> {
        const i18nkey = 'pageTitle.' + url;
        return this.translateService.get(i18nkey).pipe(
            map(translatedValue => translatedValue !== i18nkey ? translatedValue : null)
        );
    }
}
