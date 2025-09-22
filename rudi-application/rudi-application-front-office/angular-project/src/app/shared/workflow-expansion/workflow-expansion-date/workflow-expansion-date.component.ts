import {Component, Input} from '@angular/core';

@Component({
    selector: 'app-workflow-expansion-date',
    templateUrl: './workflow-expansion-date.component.html',
    styleUrl: './workflow-expansion-date.component.scss',
    standalone: false
})
export class WorkflowExpansionDateComponent {
    @Input() label: string = null;
    @Input() value: string = null;
}
