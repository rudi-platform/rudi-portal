import {Component} from '@angular/core';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {MatCheckbox} from '@angular/material/checkbox';
import {MatError} from '@angular/material/form-field';
import {DomSanitizer, SafeHtml} from '@angular/platform-browser';
import {TranslatePipe} from '@ngx-translate/core';
import {WorkflowFieldComponent} from '@shared/core/workflow/fields/workflow-field/workflow-field.component';

@Component({
    selector: 'app-workflow-field-boolean',
    templateUrl: './workflow-field-boolean.component.html',
    styleUrls: ['./workflow-field-boolean.component.scss'],
    imports: [FormsModule, ReactiveFormsModule, MatCheckbox, MatError, TranslatePipe]
})
export class WorkflowFieldBooleanComponent extends WorkflowFieldComponent {

    constructor(private readonly sanitizer: DomSanitizer) {
        super();
    }

    /**
     * Permet d'interpreter les styles css décrits dans le label depuis le back
     */
    get safeLabel(): SafeHtml {
        return this.sanitizer.bypassSecurityTrustHtml(this.field.definition.label);
    }
}
