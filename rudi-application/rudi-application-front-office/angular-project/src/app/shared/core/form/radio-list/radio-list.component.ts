import {Component, Input} from '@angular/core';
import {FormGroup, FormsModule, ReactiveFormsModule} from '@angular/forms';
import {RadioListItem} from '@shared/core/form/radio-list/radio-list-item';
import {MatRadioGroup, MatRadioButton} from '@angular/material/radio';
import { NgClass } from '@angular/common';

/**
 * Composant générique de listes de suggestions radio bindées sur FormControl
 */
@Component({
    selector: 'app-radio-list',
    templateUrl: './radio-list.component.html',
    styleUrls: ['./radio-list.component.scss'],
    imports: [FormsModule, ReactiveFormsModule, MatRadioGroup, NgClass, MatRadioButton]
})
export class RadioListComponent {

    /**
     * La liste des suggestions du groupe de boutons radio
     */
    @Input()
    public suggestions: RadioListItem[];

    @Input()
    public isConfidentialityValid: boolean;

    @Input()
    public comp: RadioListItem[];

    /**
     * FormGroup contenant le contrôle de la valeur choisie
     */
    @Input()
    public formGroup: FormGroup;

    /**
     * Le nom du contrôle du formgroup
     */
    @Input()
    public controlName: string;
}
