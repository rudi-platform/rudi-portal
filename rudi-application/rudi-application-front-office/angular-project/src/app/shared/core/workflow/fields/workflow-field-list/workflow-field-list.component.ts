import {Component, OnInit} from '@angular/core';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {MatOption} from '@angular/material/core';
import {MatError, MatFormField, MatHint} from '@angular/material/form-field';
import {MatSelect} from '@angular/material/select';
import {TranslatePipe} from '@ngx-translate/core';
import {WorkflowFieldComponent} from '@shared/core/workflow/fields/workflow-field/workflow-field.component';

@Component({
    selector: 'app-workflow-field-list',
    templateUrl: './workflow-field-list.component.html',
    styleUrls: ['./workflow-field-list.component.scss'],
    imports: [FormsModule, ReactiveFormsModule, MatHint, MatFormField, MatSelect, MatOption, MatError, TranslatePipe]
})
export class WorkflowFieldListComponent extends WorkflowFieldComponent implements OnInit {

    public options: any;

    ngOnInit(): void {
        if (this.field?.definition?.extendedType) {
            let extendedTypeString: string = this.field.definition.extendedType;

            this.options = JSON.parse(extendedTypeString);
        }
    }


}
