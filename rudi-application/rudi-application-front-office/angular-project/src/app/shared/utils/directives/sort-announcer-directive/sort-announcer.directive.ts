import {LiveAnnouncer} from '@angular/cdk/a11y';
import {Directive, ElementRef} from '@angular/core';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {MatSort, Sort} from '@angular/material/sort';
import {TranslateService} from '@ngx-translate/core';

/**
 * Announces the new sort of a table sorted client-side: no loading is announced in that case,
 * so screen reader users get no feedback after activating a sort header (RGAA 7.5).
 */
@Directive({
    selector: '[matSort][appSortAnnouncer]',
    standalone: true
})
export class SortAnnouncerDirective {

    constructor(
        matSort: MatSort,
        private readonly elementRef: ElementRef<HTMLElement>,
        private readonly liveAnnouncer: LiveAnnouncer,
        private readonly translateService: TranslateService,
    ) {
        matSort.sortChange.pipe(takeUntilDestroyed()).subscribe(sort => this.announce(sort));
    }

    private announce(sort: Sort): void {
        const header = this.elementRef.nativeElement.querySelector(`th.mat-column-${sort.active}`);
        const column = header?.textContent.replace(/\s+/g, ' ').trim() || sort.active;
        const message = this.translateService.instant(`common.sortAnnouncement.${sort.direction || 'none'}`, {column});
        this.liveAnnouncer.announce(message, 'polite');
    }
}
