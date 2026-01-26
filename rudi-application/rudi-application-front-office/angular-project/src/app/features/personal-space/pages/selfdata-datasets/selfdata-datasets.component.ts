import {NgIf} from '@angular/common';
import {Component} from '@angular/core';
import {IconRegistryService} from '@core/services/icon-registry.service';
import {PropertiesMetierService} from '@core/services/properties-metier.service';
import {TranslatePipe} from '@ngx-translate/core';
import {TabComponent} from '@shared/core/common/tab/tab.component';
import {TabsComponent} from '@shared/core/common/tabs/tabs.component';
import {PageTitleComponent} from '@shared/core/layout/page-title/page-title.component';
import {PageComponent} from '@shared/core/layout/page/page.component';
import {ALL_TYPES} from '@shared/models/title-icon-type';
import {SelfdataDatasetsTableComponent} from '../../components/selfdata-datasets-table/selfdata-datasets-table.component';

@Component({
    selector: 'app-selfdata-datasets',
    templateUrl: './selfdata-datasets.component.html',
    imports: [PageComponent, NgIf, PageTitleComponent, TabsComponent, TabComponent, SelfdataDatasetsTableComponent, TranslatePipe]
})
export class SelfdataDatasetsComponent {
    urlToDoc: string;
    searchUrlLoading = false;

    constructor(
        iconRegistryService: IconRegistryService,
        private readonly propertiesMetierService: PropertiesMetierService,
    ) {
        iconRegistryService.addAllSvgIcons(ALL_TYPES);
        this.getUrlToDoc();
    }

    /**
     * on ouvre un nouvel onglet vers la documentation de l'utilisation de l'URL
     */
    getUrlToDoc(): void {
        this.searchUrlLoading = true;
        this.propertiesMetierService.get('front.docRudi').subscribe({
            next: (link: string) => {
                this.urlToDoc = link;
                this.searchUrlLoading = false;
            },
            error: (error) => {
                this.searchUrlLoading = false;
                console.log(error);
            }
        });
    }
}
