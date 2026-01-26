import {AsyncPipe, NgClass, NgIf} from '@angular/common';
import {Component, OnDestroy, OnInit, Renderer2, ViewChild} from '@angular/core';
import {ExtendedModule} from '@angular/flex-layout/extended';
import {MatBadge} from '@angular/material/badge';
import {MatButton} from '@angular/material/button';
import {MatIcon} from '@angular/material/icon';
import {MatSidenav, MatSidenavContainer, MatSidenavContent} from '@angular/material/sidenav';
import {Router} from '@angular/router';
import {BreakpointObserverService, MediaSize} from '@core/services/breakpoint-observer.service';
import {FiltersService} from '@core/services/filters.service';
import {OrderValue} from '@core/services/filters/order-filter';
import {KonsultMetierService} from '@core/services/konsult-metier.service';
import {KosMetierService} from '@core/services/kos-metier.service';
import {ProvidersMetierService} from '@core/services/providers-metier.service';
import {SidenavOpeningsService} from '@core/services/sidenav-openings.service';
import {TranslatePipe} from '@ngx-translate/core';
import {
    AccessStatusFilterFormComponent,
    AccessStatusFilterItem
} from '@shared/business/dataset/filters/filter-forms/access-status-filter-form/access-status-filter-form.component';
import {DatesFilterFormComponent} from '@shared/business/dataset/filters/filter-forms/dates-filter-form/dates-filter-form.component';
import {Item} from '@shared/business/dataset/filters/filter-forms/item';
import {OrderFilterFormComponent} from '@shared/business/dataset/filters/filter-forms/order-filter-form/order-filter-form.component';
import {
    ProducerNamesFilterFormComponent
} from '@shared/business/dataset/filters/filter-forms/producer-names-filter-form/producer-names-filter-form.component';
import {ThemesFilterFormComponent} from '@shared/business/dataset/filters/filter-forms/themes-filter-form/themes-filter-form.component';
import {
    FilterSidenavContainerComponent
} from '@shared/business/dataset/filters/filter-sidenav-container/filter-sidenav-container.component';
import {ListContainerComponent} from '@shared/business/dataset/filters/list-container/list-container.component';
import {PageTitleComponent} from '@shared/core/layout/page-title/page-title.component';
import {SimpleSkosConcept} from 'micro_service_modules/kos/kos-model';
import {of, Subject} from 'rxjs';
import {switchMap, takeUntil} from 'rxjs/operators';

@Component({
    selector: 'app-list',
    templateUrl: './list.component.html',
    styleUrls: ['./list.component.scss'],
    imports: [MatSidenavContainer, MatSidenav, FilterSidenavContainerComponent, MatIcon, MatBadge, NgIf, MatButton, OrderFilterFormComponent, ThemesFilterFormComponent, ProducerNamesFilterFormComponent, DatesFilterFormComponent, AccessStatusFilterFormComponent, MatSidenavContent, NgClass, ExtendedModule, PageTitleComponent, ListContainerComponent, AsyncPipe, TranslatePipe]
})
export class ListComponent implements OnInit, OnDestroy {
    @ViewChild('sidenav') sidenav: MatSidenav;
    accessStatusfilterForcedValue; // positionné à undefined en attendant le mode mobile de ../../components/list-container

    mediaSize: MediaSize;
    themes: SimpleSkosConcept[];
    orders: OrderValue[] = [
        'resource_title',
        '-resource_title',
        'dataset_dates.created',
        '-dataset_dates.created',
        'producer.organization_name',
        '-producer.organization_name',
    ];

    producerNames: string[];
    selectedDatesItems: Item[] = [];
    selectedThemeItems: Item[] = [];
    selectedProducerItems: Item[] = [];

    selectedAccessStatusFilterItems: AccessStatusFilterItem[] = [];
    private isDestroyed$: Subject<void> = new Subject<void>();

    constructor(
        private readonly konsultMetierService: KonsultMetierService,
        private readonly kosMetierService: KosMetierService,
        private readonly router: Router,
        private readonly filtersService: FiltersService,
        private readonly breakpointObserver: BreakpointObserverService,
        private readonly providersMetierService: ProvidersMetierService,
        private readonly sidenavOpeningsService: SidenavOpeningsService,
        private readonly renderer: Renderer2
    ) {
    }

    ngOnInit(): void {
        this.mediaSize = this.breakpointObserver.getMediaSize();
        this.sidenavOpeningsService.sideNavOpening$.pipe(takeUntil(this.isDestroyed$)).subscribe(() => {
            this.sidenav.open();
        });
        this.konsultMetierService.getProducerNames().subscribe(
            producerNames => this.producerNames = producerNames
        );
        this.konsultMetierService.getThemeCodes().pipe(
            switchMap(themeCodes => themeCodes.length > 0 ? this.kosMetierService.getThemes(themeCodes) : of([]))
        ).subscribe(concepts => {
            this.themes = concepts;
        });
    }

    ngOnDestroy(): void {
        this.isDestroyed$.next();
        this.renderer.removeClass(document.body, 'menu');
    }

    submitFilters(): void {
        this.filtersService.deleteAllFilters();
        this.sidenav.close();
    }
}
