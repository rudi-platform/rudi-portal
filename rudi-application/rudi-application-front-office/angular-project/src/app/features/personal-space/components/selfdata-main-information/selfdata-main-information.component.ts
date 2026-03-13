
import {Component, Input} from '@angular/core';
import {TranslateDirective, TranslatePipe} from '@ngx-translate/core';
import {CardComponent} from '@shared/core/common/card/card.component';
import {LoaderComponent} from '@shared/core/common/loader/loader.component';
import {WorkflowFormComponent} from '@shared/core/workflow/forms/workflow-form/workflow-form.component';
import {ProcessDefinitionKeyTranslatePipe} from '@shared/utils/pipes/process-definition-key-translate.pipe';
import {Form} from 'micro_service_modules/selfdata/selfdata-api';
import {RequestDetailDependencies} from '../../pages/request-detail-dependencies';

@Component({
    selector: 'app-selfdata-main-information',
    templateUrl: './selfdata-main-information.component.html',
    styleUrls: ['./selfdata-main-information.component.scss'],
    imports: [CardComponent, LoaderComponent, TranslateDirective, WorkflowFormComponent, TranslatePipe, ProcessDefinitionKeyTranslatePipe]
})
export class SelfdataMainInformationComponent {
    @Input() task: RequestDetailDependencies;
    @Input() taskLoading: boolean;
    @Input() formLoading: boolean;
    @Input() form: Form;
}
