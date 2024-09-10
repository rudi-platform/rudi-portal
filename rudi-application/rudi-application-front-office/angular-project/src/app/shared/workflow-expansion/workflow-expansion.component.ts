import {Component, signal, Input} from '@angular/core';
import {FieldType, Section} from 'micro_service_modules/projekt/projekt-api';

@Component({
  selector: 'app-workflow-expansion',
  templateUrl: './workflow-expansion.component.html',
  styleUrl: './workflow-expansion.component.scss'
})
export class WorkflowExpansionComponent {

    readonly panelTaskOpenState = signal(true);
    fieldType = FieldType;
    @Input() section: Section;

    getFieldListLabel(extendedType: string, values: string[]): any  {
            // Convertir extendedType en tableau d'objets
            const extendedTypeArray = JSON.parse(extendedType);
            // Filtrer et mapper les valeurs pour obtenir les labels correspondants
            const labels = values.map(value => {
            // Trouver l'objet dans extendedTypeArray dont le code correspond au value actuel
            const foundObject = extendedTypeArray.find(item => item.code === value);
            // Retourner le label correspondant, ou undefined si le code n'est pas trouvé
            return foundObject ?  " " + foundObject.label : undefined;
        });
        return labels;
    }
}
