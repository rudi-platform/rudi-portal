import {NgIf} from '@angular/common';
import {Component} from '@angular/core';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {MatError, MatFormField, MatHint, MatLabel} from '@angular/material/form-field';
import {MatInput} from '@angular/material/input';
import {TranslatePipe} from '@ngx-translate/core';
import {WorkflowFieldComponent} from '@shared/core/workflow/fields/workflow-field/workflow-field.component';

@Component({
    selector: 'app-workflow-field-text',
    templateUrl: './workflow-field-text.component.html',
    styleUrls: ['./workflow-field-text.component.scss'],
    imports: [FormsModule, ReactiveFormsModule, MatLabel, NgIf, MatHint, MatFormField, MatInput, MatError, TranslatePipe]
})
export class WorkflowFieldTextComponent extends WorkflowFieldComponent {

    get fullLabel() {
        return this.label ? this.label + ' : ' : '';
    }
}
