import {Component} from '@angular/core';
import {WorkflowFieldComponent} from '@shared/workflow-field/workflow-field.component';

@Component({
    selector: 'app-workflow-field-hidden',
    templateUrl: './workflow-field-hidden.component.html',
    styleUrls: ['./workflow-field-hidden.component.scss'],
    standalone: false
})
export class WorkflowFieldHiddenComponent extends WorkflowFieldComponent {
}
