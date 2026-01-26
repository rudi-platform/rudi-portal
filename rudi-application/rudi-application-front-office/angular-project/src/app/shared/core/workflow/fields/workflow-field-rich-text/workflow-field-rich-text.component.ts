import { NgIf } from '@angular/common';
import {Component} from '@angular/core';
import {RichTextEditorComponent} from '@shared/core/common/rich-text-editor/rich-text-editor.component';
import {WorkflowFieldComponent} from '@shared/core/workflow/fields/workflow-field/workflow-field.component';
import {MaterialModules} from '@shared/shared.constant';

@Component({
    selector: 'app-workflow-field-rich-text',
    imports: [
        RichTextEditorComponent,
        MaterialModules,
        NgIf
    ],
    templateUrl: './workflow-field-rich-text.component.html',
    styleUrl: './workflow-field-rich-text.component.scss'
})
export class WorkflowFieldRichTextComponent extends WorkflowFieldComponent {

}
