import {Component, Input} from '@angular/core';

@Component({
  selector: 'app-workflow-expansion-label',
  templateUrl: './workflow-expansion-label.component.html',
  styleUrl: './workflow-expansion-label.component.scss'
})
export class WorkflowExpansionLabelComponent {
    @Input() label: string;
    @Input() value: string;
}
