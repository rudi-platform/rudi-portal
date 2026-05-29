import {Component, OnInit} from '@angular/core';
import {LogService} from '@core/services/log.service';
import {PropertiesMetierService} from '@core/services/properties-metier.service';
import {TranslatePipe} from '@ngx-translate/core';
import {LoaderComponent} from '@shared/core/common/loader/loader.component';
import {TabComponent} from '@shared/core/common/tab/tab.component';
import {TabsComponent} from '@shared/core/common/tabs/tabs.component';
import {PageTitleComponent} from '@shared/core/layout/page-title/page-title.component';
import {PageComponent} from '@shared/core/layout/page/page.component';
import {MyProfilComponent} from '../../components/my-profil/my-profil.component';
import {OrganizationTabComponent} from '../../components/organization-tab/organization-tab.component';

@Component({
    selector: 'app-my-account',
    templateUrl: './my-account.component.html',
    styleUrls: ['./my-account.component.scss'],
    imports: [
        PageComponent,
        LoaderComponent,
        PageTitleComponent,
        TabsComponent,
        TabComponent,
        MyProfilComponent,
        OrganizationTabComponent,
        TranslatePipe,
    ]
})
export class MyAccountComponent implements OnInit {
    isLoading: boolean;
    urlToDoc: string;

    constructor(
        private readonly propertiesMetierService: PropertiesMetierService,
        private readonly logService: LogService
    ) {
    }

    ngOnInit(): void {
        this.isLoading = true;
        this.propertiesMetierService.get('front.docRudi').subscribe({
            next: (rudiDocLink: string) => {
                this.urlToDoc = rudiDocLink;
                this.isLoading = false;
            },
            error: (err) => {
                this.logService.error(err);
                this.isLoading = false;
            }
        });
    }
}
