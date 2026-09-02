import {AfterViewInit, Component, ElementRef, ViewChild} from '@angular/core';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {MatError, MatFormField, MatHint} from '@angular/material/form-field';
import {MatInput} from '@angular/material/input';
import {TranslatePipe} from '@ngx-translate/core';
import {WorkflowFieldComponent} from '@shared/core/workflow/fields/workflow-field/workflow-field.component';

/**
 * Champ dédié à la saisie d'un numéro de téléphone français.
 * Réutilise la logique de {@link WorkflowFieldComponent} et formate la saisie :
 * chiffres uniquement, 10 chiffres maximum, un espace tous les 2 chiffres (ex : {@code 06 12 34 56 78}).
 */
@Component({
    selector: 'app-workflow-field-phone',
    templateUrl: './workflow-field-phone.component.html',
    styleUrls: ['../workflow-field/workflow-field.component.scss'],
    imports: [FormsModule, ReactiveFormsModule, MatFormField, MatInput, MatError, MatHint, TranslatePipe]
})
export class WorkflowFieldPhoneComponent extends WorkflowFieldComponent implements AfterViewInit {
    private static readonly MAX_DIGITS = 10;

    @ViewChild('phoneInput')
    phoneInput?: ElementRef<HTMLInputElement>;

    ngAfterViewInit(): void {
        this.onPhoneInput();
    }

    onPhoneInput(): void {
        const input = this.phoneInput?.nativeElement;
        if (!input) {
            return;
        }

        const digits = (input.value ?? '')
            .replace(/\D/g, '')
            .slice(0, WorkflowFieldPhoneComponent.MAX_DIGITS);
        const formatted = digits.replace(/(\d{2})(?=\d)/g, '$1 ');

        if (formatted === input.value) {
            return;
        }

        input.value = formatted;
        this.formControl?.setValue(formatted, {emitEvent: false});
    }
}
