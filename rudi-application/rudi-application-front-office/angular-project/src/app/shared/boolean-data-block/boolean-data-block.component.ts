import {Component, Input} from '@angular/core';
import {IconRegistryService} from '@core/services/icon-registry.service';
import {ALL_TYPES} from '../models/title-icon-type';

@Component({
    selector: 'app-boolean-data-block',
    templateUrl: './boolean-data-block.component.html',
    styleUrls: ['./boolean-data-block.component.scss'],
    standalone: false
})
export class BooleanDataBlockComponent {

    /**
     * la valeur du booléen, true ou false
     */
    @Input() booleanValue: boolean;

    /**
     * le libellé : "Données de santé"
     */
    @Input() libelle: string;

    constructor(
        private readonly iconRegistryService: IconRegistryService,
    ) {
        iconRegistryService.addAllSvgIcons(ALL_TYPES);
    }

    /**
     * Icon de la donée
     */
    get icon(): string {
        if (this.booleanValue) {
            return 'coche-verte';
        } else {
            return 'croix-rouge';
        }
    }

}
