import {Component, OnInit} from '@angular/core';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {MatError, MatFormField, MatHint} from '@angular/material/form-field';
import {MatInput} from '@angular/material/input';
import {TranslatePipe} from '@ngx-translate/core';
import {WorkflowFieldComponent} from '@shared/core/workflow/fields/workflow-field/workflow-field.component';
import {DateTimeUtils} from '@shared/utils/date-time-utils';

@Component({
    selector: 'app-workflow-field-date',
    templateUrl: './workflow-field-date.component.html',
    styleUrls: ['./workflow-field-date.component.scss'],
    imports: [FormsModule, ReactiveFormsModule, MatHint, MatFormField, MatInput, MatError, TranslatePipe]
})
export class WorkflowFieldDateComponent extends WorkflowFieldComponent implements OnInit {
    ngOnInit(): void {
        this.formControl.patchValue(DateTimeUtils.formatStringDate(this.formControl.value));
    }
}
