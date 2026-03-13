
import {Component} from '@angular/core';
import {PropertiesMetierService} from '@core/services/properties-metier.service';
import {TranslatePipe} from '@ngx-translate/core';
import {LoaderComponent} from '@shared/core/common/loader/loader.component';
import {TabComponent} from '@shared/core/common/tab/tab.component';
import {TabsComponent} from '@shared/core/common/tabs/tabs.component';
import {PageTitleComponent} from '@shared/core/layout/page-title/page-title.component';
import {PageComponent} from '@shared/core/layout/page/page.component';
import {MyRequestsComponent} from '../../components/my-requests/my-requests.component';
import {ReusesComponent} from '../../components/reuses/reuses.component';

@Component({
    selector: 'app-my-activity',
    templateUrl: './my-activity.component.html',
    imports: [PageComponent, LoaderComponent, PageTitleComponent, TabsComponent, TabComponent, ReusesComponent, MyRequestsComponent, TranslatePipe]
})
export class MyActivityComponent {
    urlToDoc: string;
    searchUrlLoading = false;

    constructor(private readonly propertiesMetierService: PropertiesMetierService,) {
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
