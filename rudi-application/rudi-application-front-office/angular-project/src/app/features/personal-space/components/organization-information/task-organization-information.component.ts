import {Component, inject, Input, OnInit} from '@angular/core';
import {MatCard, MatCardContent} from '@angular/material/card';
import {Base64EncodedLogo} from '@core/services/image-logo.service';
import {OrganizationAddresses} from '@core/services/organization/organization-addresses';
import {OrganizationAddressesMapper} from '@core/services/organization/organization-addresses-mapper';
import {TranslatePipe} from '@ngx-translate/core';
import {LoaderComponent} from '@shared/core/common/loader/loader.component';
import {Organization} from 'micro_service_modules/strukture/strukture-model';

@Component({
    selector: 'app-task-organization-information',
    templateUrl: './task-organization-information.component.html',
    styleUrls: ['./task-organization-information.component.scss'],
    imports: [MatCard, LoaderComponent, MatCardContent, TranslatePipe]
})
export class TaskOrganizationInformationComponent implements OnInit {
    @Input()
    organization: Organization;
    @Input()
    loading: boolean;
    @Input()
    imageBase64: Base64EncodedLogo;

    organizationAddressesMapper = inject(OrganizationAddressesMapper);

    organizationAddresses?: OrganizationAddresses;

    ngOnInit(): void {
        this.organization = {
            ...this.organization as Organization,
        };
        console.log('Organization', this.organization);
        console.log('Organization.addresses', this.organization.addresses);
        this.organizationAddresses = this.organizationAddressesMapper.mapAbstractAddressesToOrganizationAddresses(this.organization.addresses);
        console.log('OrganizationAddresses', this.organizationAddresses);
    }


}
