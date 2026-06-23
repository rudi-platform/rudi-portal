import {HttpErrorResponse} from '@angular/common/http';
import {Component, OnInit} from '@angular/core';
import {ActivatedRoute, Params, Router} from '@angular/router';
import {AuthenticationService} from '@core/services/authentication.service';
import {LogService} from '@core/services/log.service';
import {OrganizationMetierService} from '@core/services/organization/organization-metier.service';
import {PageTitleService} from '@core/services/page-title.service';
import {UserService} from '@core/services/user.service';
import {TranslatePipe, TranslateService} from '@ngx-translate/core';
import {TabComponent} from '@shared/core/common/tab/tab.component';
import {TabsComponent} from '@shared/core/common/tabs/tabs.component';
import {PageHeadingComponent} from '@shared/core/layout/page-heading/page-heading.component';
import {PageComponent} from '@shared/core/layout/page/page.component';
import {Organization, OrganizationStatus} from 'micro_service_modules/strukture/strukture-model';
import {switchMap, tap} from 'rxjs/operators';
import {AdministrationTabComponent} from '../../components/administration-tab/administration-tab.component';
import {OrganizationInformationsComponent} from '../../components/organization-informations/organization-informations.component';


@Component({
    selector: 'app-detail',
    templateUrl: './detail.component.html',
    imports: [PageComponent, PageHeadingComponent, TabsComponent,
        TabComponent, OrganizationInformationsComponent, AdministrationTabComponent, TranslatePipe]
})
export class DetailComponent implements OnInit {

    public isLoading: boolean;
    public organization: Organization;
    public _displayAdministrationTab: boolean;

    constructor(private readonly route: ActivatedRoute,
                private readonly router: Router,
                private readonly organizationService: OrganizationMetierService,
                private readonly userService: UserService,
                private readonly authenticationService: AuthenticationService,
                private readonly logService: LogService,
                private readonly pageTitleService: PageTitleService,
                private readonly translateService: TranslateService) {
    }

    ngOnInit(): void {
        this.route.params.pipe(
            switchMap((params: Params) => {
                // Si uuid, on charge l'organization
                if (params.organizationUuid) {
                    this.isLoading = true;
                    return this.organizationService.getOrganizationByUuid(params.organizationUuid);
                } else {
                    // Sinon erreur on peut pas afficher la page
                    throw Error('Erreur pas d\'UUID de d\'organisation');
                }
            }),
            tap((organization: Organization) => {
                const isAuth = this.authenticationService.isAuthenticatedAsUser();
                if (isAuth) {
                    this.isAdministrator(organization.uuid);
                }
            })
        ).subscribe(
            {
                next: (organization: Organization) => {
                    if (organization.name) {
                        this.pageTitleService.setPageTitle(organization.name, this.translateService.instant('pageTitle.defaultDetail'));
                    } else {
                        this.pageTitleService.setPageTitleFromUrl('/organization');
                    }
                    this.isLoading = false;
                    this.organization = organization;
                },
                error: (error: HttpErrorResponse) => {
                    this.logService.error(error);
                    if (error.status == 400) {
                        this.router.navigate(['/error/400']);
                    }
                    if (error.status == 404) {
                        this.router.navigate(['/error/404']);
                    }
                    if (error.status == 401) {
                        this.router.navigate(['/error/401']);
                    }
                    this.isLoading = false;
                }
            }
        );
    }

    get displayAdministrationTab(): boolean {
        return this._displayAdministrationTab;
    }

    isAdministrator(organizationUuid: string): void {
        this.organizationService.isAdministrator(organizationUuid)
            .subscribe({
                next: (isAdministrator: boolean) => {
                    this._displayAdministrationTab = isAdministrator;
                },
                error: (error) => {
                    this.logService.error(error);
                }
            });
    }

    protected getStatus(): string {
        if (this.organization?.organizationStatus == OrganizationStatus.Disengaged) {
            return this.translateService.instant('organization.status.disengaged');
        }

        return undefined;
    }

}
