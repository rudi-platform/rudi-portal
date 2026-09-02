import {Component, Input, signal} from '@angular/core';
import {MatAccordion, MatExpansionPanel, MatExpansionPanelHeader, MatExpansionPanelTitle} from '@angular/material/expansion';
import {TranslatePipe} from '@ngx-translate/core';
import {FieldType, Section} from 'micro_service_modules/projekt/projekt-api';
import {WorkflowExpansionDateComponent} from './workflow-expansion-date/workflow-expansion-date.component';
import {WorkflowExpansionImageComponent} from './workflow-expansion-image/workflow-expansion-image.component';
import {WorkflowExpansionLabelComponent} from './workflow-expansion-label/workflow-expansion-label.component';
import { WorklfowExpansionRichLabelComponent } from './worklfow-expansion-rich-label/worklfow-expansion-rich-label.component';

/**
 * Représentation aplatie d'un champ prête à être affichée dans le template,
 * afin d'éviter la manipulation de `field.values[0]` / `field.definition.*` dans la vue.
 */
export interface WorkflowExpansionField {
    type: FieldType;
    label: string;
    value: string;
}

@Component({
    selector: 'app-workflow-expansion',
    templateUrl: './workflow-expansion.component.html',
    styleUrl: './workflow-expansion.component.scss',
    imports: [
        MatAccordion,
        MatExpansionPanel,
        MatExpansionPanelHeader,
        MatExpansionPanelTitle,
        WorkflowExpansionLabelComponent,
        WorkflowExpansionDateComponent,
        WorkflowExpansionImageComponent,
        WorklfowExpansionRichLabelComponent,
        TranslatePipe
    ]
})
export class WorkflowExpansionComponent {

    readonly panelTaskOpenState = signal(true);
    fieldType = FieldType;
    @Input() title: string;
    @Input() pictureUuid: string;
    @Input() expanded = true;
    @Input() fieldLabel: string;

    private _section: Section;
    /** Champs prêts à l'affichage, calculés à partir de la section reçue. */
    displayFields: WorkflowExpansionField[] = [];
    /** Libellé du panneau : label de la section, sinon label du premier champ. */
    panelTitle: string;

    @Input()
    set section(section: Section) {
        this._section = section;
        this.panelTitle = section?.label ?? section?.fields?.[0]?.definition?.label;
        this.displayFields = (section?.fields ?? [])
            .filter(field => field.values)
            .map(field => ({
                type: field.definition?.type,
                label: field.definition?.label,
                value: field.definition?.type === FieldType.List
                    ? this.getFieldListLabel(field.definition.extendedType, field.values).join(',')
                    : field.values[0]
            }));
    }

    get section(): Section {
        return this._section;
    }

    private getFieldListLabel(extendedType: string, values: string[]): string[] {
        // Convertir extendedType en tableau d'objets
        const extendedTypeArray = JSON.parse(extendedType);
        // Filtrer et mapper les valeurs pour obtenir les labels correspondants
        return values.map(value => {
            // Trouver l'objet dans extendedTypeArray dont le code correspond au value actuel
            const foundObject = extendedTypeArray.find(item => item.code === value);
            // Retourner le label correspondant, ou undefined si le code n'est pas trouvé
            return foundObject ? ' ' + foundObject.label : undefined;
        });
    }
}
