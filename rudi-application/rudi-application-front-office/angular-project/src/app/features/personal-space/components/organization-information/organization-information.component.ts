import {Component, Input} from '@angular/core';
import {Organization} from 'micro_service_modules/strukture/strukture-model';

@Component({
    selector: 'app-organization-information',
    templateUrl: './organization-information.component.html',
    styleUrls: ['./organization-information.component.scss']
})
export class OrganizationInformationComponent {
    @Input()
    organization: Organization;
    @Input()
    loading: boolean;
}
