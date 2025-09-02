import {Component, OnInit} from '@angular/core';
import {WorkflowFieldComponent} from '@shared/workflow-field/workflow-field.component';

@Component({
    selector: 'app-workflow-field-list',
    templateUrl: './workflow-field-list.component.html',
    styleUrls: ['./workflow-field-list.component.scss']
})
export class WorkflowFieldListComponent extends WorkflowFieldComponent implements OnInit {

    public options: any;

    ngOnInit(): void {
        if (this.field && this.field.definition && this.field.definition.extendedType) {
            let extendedTypeString: string = this.field.definition.extendedType;

            this.options = JSON.parse(extendedTypeString);
        }
    }


}
