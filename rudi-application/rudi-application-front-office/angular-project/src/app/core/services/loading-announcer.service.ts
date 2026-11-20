import {LiveAnnouncer} from '@angular/cdk/a11y';
import {Injectable} from '@angular/core';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {TranslateService} from '@ngx-translate/core';
import {BehaviorSubject} from 'rxjs';
import {debounceTime, distinctUntilChanged, filter, map, switchMap} from 'rxjs/operators';

/** Délai de stabilisation : évite d'annoncer les chargements éclairs et regroupe les loaders simultanés ou enchaînés */
export const LOADING_ANNOUNCE_DELAY_MS = 300;

/**
 * Annonce aux technologies d'assistance le début d'un chargement (RGAA 7.5 / WCAG 4.1.3), puis sa fin.
 * Tous les loaders actifs sont agrégés : une seule annonce tant qu'au moins un loader tourne.
 */
@Injectable({
    providedIn: 'root'
})
export class LoadingAnnouncerService {
    private readonly activeLoadersCount$ = new BehaviorSubject<number>(0);
    private isLoadingAnnounced = false;

    constructor(
        private readonly liveAnnouncer: LiveAnnouncer,
        private readonly translateService: TranslateService,
    ) {
        this.activeLoadersCount$.pipe(
            map(count => count > 0),
            debounceTime(LOADING_ANNOUNCE_DELAY_MS),
            distinctUntilChanged(),
            map(isLoading => this.getAnnouncementKey(isLoading)),
            filter((key): key is string => key !== null),
            switchMap(key => this.translateService.get(key)),
            takeUntilDestroyed(),
        ).subscribe(message => this.liveAnnouncer.announce(message, 'polite'));
    }

    startLoading(): void {
        this.activeLoadersCount$.next(this.activeLoadersCount$.value + 1);
    }

    stopLoading(): void {
        this.activeLoadersCount$.next(Math.max(0, this.activeLoadersCount$.value - 1));
    }

    private getAnnouncementKey(isLoading: boolean): string | null {
        const wasLoadingAnnounced = this.isLoadingAnnounced;
        this.isLoadingAnnounced = isLoading;
        if (isLoading) {
            return 'common.loadingAnnouncement';
        }
        return wasLoadingAnnounced ? 'common.loadedAnnouncement' : null;
    }
}
