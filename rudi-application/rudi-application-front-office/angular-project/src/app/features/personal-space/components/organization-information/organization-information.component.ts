
import {Component, Input} from '@angular/core';
import {MatCard, MatCardContent} from '@angular/material/card';
import {TranslatePipe} from '@ngx-translate/core';
import {LoaderComponent} from '@shared/core/common/loader/loader.component';
import {Organization} from 'micro_service_modules/strukture/strukture-model';

@Component({
    selector: 'app-organization-information',
    templateUrl: './organization-information.component.html',
    styleUrls: ['./organization-information.component.scss'],
    imports: [MatCard, LoaderComponent, MatCardContent, TranslatePipe]
})
export class OrganizationInformationComponent {
    @Input()
    organization: Organization;
    @Input()
    loading: boolean;
    @Input()
    imageBase64: string;
}
