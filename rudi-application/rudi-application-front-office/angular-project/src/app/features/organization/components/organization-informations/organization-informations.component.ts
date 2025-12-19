import {Component, Input, OnInit} from '@angular/core';
import {DEFAULT_PROJECT_ORDER} from '@core/services/asset/project/projekt-metier.service';
import {BreakpointObserverService, MediaSize} from '@core/services/breakpoint-observer.service';
import {FiltersService} from '@core/services/filters.service';
import {OrganizationMetierService} from '@core/services/organization/organization-metier.service';
import {Organization} from 'micro_service_modules/strukture/strukture-model';


@Component({
    selector: 'app-organization-informations',
    templateUrl: './organization-informations.component.html',
    styleUrls: ['./organization-informations.component.scss'],
    standalone: false
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
    isMember: boolean = false;
    isMemberLoading = false;


    constructor(private readonly filtersService: FiltersService,
                private readonly breakpointObserver: BreakpointObserverService,
                private readonly organizationMetierService: OrganizationMetierService
    ) {
        this.mediaSize = this.breakpointObserver.getMediaSize();
    }

    ngOnInit(): void {
        this.isMemberLoading = true;
        this.organizationMetierService.isMember(this.organization.uuid).subscribe({
            next: (isMember) => {
                this.isMember = isMember;
                this.isMemberLoading = false;
            },
            error: (e) => {
                this.isMemberLoading = false;
            }
        })
        ;
    }


    setMetadataListTotal($event: number): void {
        this.metadataListTotal = $event;
    }

    setReuseListTotal($event: number): void {
        this.reuseListTotal = $event;
    }
}
