import {Component, Input, OnInit} from '@angular/core';
import {DEFAULT_PROJECT_ORDER} from '@core/services/asset/project/projekt-metier.service';
import {AuthenticationService} from '@core/services/authentication.service';
import {BreakpointObserverService, MediaSize} from '@core/services/breakpoint-observer.service';
import {FiltersService} from '@core/services/filters.service';
import {OrganizationAddresses} from '@core/services/organization/organization-addresses';
import {OrganizationAddressesMapper} from '@core/services/organization/organization-addresses-mapper';
import {OrganizationMetierService} from '@core/services/organization/organization-metier.service';
import {TranslatePipe} from '@ngx-translate/core';
import {ContactCardComponent} from '@shared/business/contacts/contact-card/contact-card.component';
import {DatasetListComponent} from '@shared/business/dataset/common/dataset-list/dataset-list.component';
import {ProjectListComponent} from '@shared/business/projects/project-list/project-list.component';
import {CardComponent} from '@shared/core/common/card/card.component';
import {LoaderComponent} from '@shared/core/common/loader/loader.component';
import {Organization} from 'micro_service_modules/strukture/strukture-model';


@Component({
    selector: 'app-organization-informations',
    templateUrl: './organization-informations.component.html',
    styleUrls: ['./organization-informations.component.scss'],
    imports: [LoaderComponent, CardComponent, ContactCardComponent, DatasetListComponent, ProjectListComponent, TranslatePipe]
})
export class OrganizationInformationsComponent implements OnInit {
    @Input() isLoading: boolean;
    @Input() organization: Organization;
    limit = 9;
    mediaSize: MediaSize;
    metadataListTotal: number;
    searchIsRunning = true;
    projectListTotal = 0;
    order = DEFAULT_PROJECT_ORDER;
    reuseListTotal: number;
    isMember = false;
    isMemberLoading = false;
    organizationAddresses?: OrganizationAddresses;


    constructor(private readonly filtersService: FiltersService,
                private readonly breakpointObserver: BreakpointObserverService,
                private readonly organizationMetierService: OrganizationMetierService,
                private readonly authenticationService: AuthenticationService,
                private readonly organizationAddressesMapper: OrganizationAddressesMapper,
    ) {
        this.mediaSize = this.breakpointObserver.getMediaSize();
    }

    ngOnInit(): void {
        this.organization = {
            ...this.organization
        };

        // Récupération des addresses à afficher dans les cards de contactàà
        this.organizationAddresses = this.organizationAddressesMapper.mapAbstractAddressesToOrganizationAddresses(this.organization.addresses);

        const isAuth = this.authenticationService.isAuthenticatedAsUser();
        if (isAuth) {
            this.isMemberLoading = true;
            this.organizationMetierService.isMember(this.organization.uuid).subscribe({
                next: (isMember) => {
                    this.isMember = isMember;
                    this.isMemberLoading = false;
                },
                error: (e) => {
                    this.isMemberLoading = false;
                }
            });
        } else {
            this.isMemberLoading = false;
        }
    }

    setMetadataListTotal($event: number): void {
        this.metadataListTotal = $event;
    }

    setReuseListTotal($event: number): void {
        this.reuseListTotal = $event;
    }
}
