import {Component, Input} from '@angular/core';
import {Form} from 'micro_service_modules/selfdata/selfdata-api';
import {RequestDetailDependencies} from '../../pages/request-detail-dependencies';

@Component({
    selector: 'app-selfdata-main-information',
    templateUrl: './selfdata-main-information.component.html',
    styleUrls: ['./selfdata-main-information.component.scss'],
    standalone: false
})
export class SelfdataMainInformationComponent {
    @Input() task: RequestDetailDependencies;
    @Input() taskLoading: boolean;
    @Input() formLoading: boolean;
    @Input() form: Form;
}
