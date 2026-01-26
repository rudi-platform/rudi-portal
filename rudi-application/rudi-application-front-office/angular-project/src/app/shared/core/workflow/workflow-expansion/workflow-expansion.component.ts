import {NgFor, NgIf, NgSwitch, NgSwitchCase, NgSwitchDefault} from '@angular/common';
import {Component, Input, signal} from '@angular/core';
import {MatAccordion, MatExpansionPanel, MatExpansionPanelHeader, MatExpansionPanelTitle} from '@angular/material/expansion';
import {FieldType, Section} from 'micro_service_modules/projekt/projekt-api';
import {WorkflowExpansionDateComponent} from './workflow-expansion-date/workflow-expansion-date.component';
import {WorkflowExpansionLabelComponent} from './workflow-expansion-label/workflow-expansion-label.component';
import { WorklfowExpansionRichLabelComponent } from './worklfow-expansion-rich-label/worklfow-expansion-rich-label.component';

@Component({
    selector: 'app-workflow-expansion',
    templateUrl: './workflow-expansion.component.html',
    styleUrl: './workflow-expansion.component.scss',
    imports: [MatAccordion, MatExpansionPanel, MatExpansionPanelHeader, MatExpansionPanelTitle, NgFor, NgIf, NgSwitch, NgSwitchCase, 
        WorkflowExpansionLabelComponent, WorkflowExpansionDateComponent, WorklfowExpansionRichLabelComponent, NgSwitchDefault]
})
export class WorkflowExpansionComponent {

    readonly panelTaskOpenState = signal(true);
    fieldType = FieldType;
    @Input() section: Section;
    @Input() title: string;

    getFieldListLabel(extendedType: string, values: string[]): any {
        // Convertir extendedType en tableau d'objets
        const extendedTypeArray = JSON.parse(extendedType);
        // Filtrer et mapper les valeurs pour obtenir les labels correspondants
        const labels = values.map(value => {
            // Trouver l'objet dans extendedTypeArray dont le code correspond au value actuel
            const foundObject = extendedTypeArray.find(item => item.code === value);
            // Retourner le label correspondant, ou undefined si le code n'est pas trouvé
            return foundObject ? ' ' + foundObject.label : undefined;
        });
        return labels;
    }


    protected readonly Text = Text;
}
