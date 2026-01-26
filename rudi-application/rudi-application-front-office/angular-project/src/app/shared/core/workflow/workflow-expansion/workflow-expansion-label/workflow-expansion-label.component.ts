import {NgIf} from '@angular/common';
import {Component, Input} from '@angular/core';
import {MatLabel} from '@angular/material/form-field';

@Component({
    selector: 'app-workflow-expansion-label',
    templateUrl: './workflow-expansion-label.component.html',
    styleUrl: './workflow-expansion-label.component.scss',
    imports: [NgIf, MatLabel]
})
export class WorkflowExpansionLabelComponent {
    @Input() label: string;
    @Input() value: string;
}
