import {DatePipe} from '@angular/common';
import {Component, Input} from '@angular/core';
import {WorkflowExpansionLabelComponent} from '../workflow-expansion-label/workflow-expansion-label.component';

@Component({
    selector: 'app-workflow-expansion-date',
    templateUrl: './workflow-expansion-date.component.html',
    styleUrl: './workflow-expansion-date.component.scss',
    imports: [WorkflowExpansionLabelComponent, DatePipe]
})
export class WorkflowExpansionDateComponent {
    @Input() label: string = null;
    @Input() value: string = null;
}
