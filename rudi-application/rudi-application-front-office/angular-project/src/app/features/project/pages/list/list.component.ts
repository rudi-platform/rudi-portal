import {NgClass} from '@angular/common';
import {Component, OnInit} from '@angular/core';
import {MatSidenavContainer, MatSidenavContent} from '@angular/material/sidenav';
import {Router} from '@angular/router';
import {DEFAULT_PROJECT_ORDER} from '@core/services/asset/project/projekt-metier.service';
import {BreakpointObserverService, MediaSize} from '@core/services/breakpoint-observer.service';
import {URIComponentCodec} from '@core/services/codecs/uri-component-codec';
import {FiltersService} from '@core/services/filters.service';
import {TranslatePipe} from '@ngx-translate/core';
import {ProjectListComponent} from '@shared/business/projects/project-list/project-list.component';
import {PageTitleComponent} from '@shared/core/layout/page-title/page-title.component';
import {SearchBoxComponent} from '@shared/core/search/search-box/search-box.component';
import {BannerComponent} from '../../components/banner/banner.component';
import {OrderComponent} from '../../components/order/order.component';
import {ProjectCatalogItem, ProjectCatalogItemPage} from '../../model/project-catalog-item';

@Component({
    selector: 'app-list',
    templateUrl: './list.component.html',
    styleUrls: ['./list.component.scss'],
    imports: [MatSidenavContainer, MatSidenavContent, NgClass, PageTitleComponent,
        BannerComponent, OrderComponent, ProjectListComponent, TranslatePipe, SearchBoxComponent]
})
export class ListComponent implements OnInit {
    mediaSize: MediaSize;
    order = DEFAULT_PROJECT_ORDER;
    freeText = '';
    displayedTotal: number | null = null;
    searchedTerm = '';

    get resultsForParams(): { term: string } {
        return {term: this.searchedTerm};
    }

    constructor(private readonly breakpointObserver: BreakpointObserverService,
                private readonly router: Router,
                private readonly uriComponentCodec: URIComponentCodec,
                private readonly filtersService: FiltersService,
    ) {
    }

    ngOnInit(): void {
        // Le terme de recherche est partagé via FiltersService (singleton) et persiste d'une page à l'autre.
        // On le réinitialise à l'arrivée sur la page Réutilisations pour repartir d'un champ vide.
        this.filtersService.searchFilter.clear();
        this.freeText = '';
        this.mediaSize = this.breakpointObserver.getMediaSize();
    }

    onProjectListChange(projectList: ProjectCatalogItemPage): void {
        // Captured with the total so the live region never pairs a new search term with the previous count
        this.displayedTotal = projectList.total;
        this.searchedTerm = this.freeText;
    }

    onClickProject(project: ProjectCatalogItem): Promise<boolean> {
        return this.router.navigate(['/projets/detail/' + project.project.uuid + '/' + this.uriComponentCodec.normalizeString(project.project.title)]);

    }

}
