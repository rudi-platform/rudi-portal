import {NgClass} from '@angular/common';
import {Component, EventEmitter, Input, Output} from '@angular/core';
import {FormsModule} from '@angular/forms';
import {MatIcon} from '@angular/material/icon';
import {IconRegistryService} from '@core/services/icon-registry.service';
import {TranslatePipe} from '@ngx-translate/core';
import {ALL_TYPES} from '@shared/models/title-icon-type';

const EMPTY_SEARCH = '';

@Component({
    selector: 'app-spreadsheet-search',
    templateUrl: './spreadsheet-search.component.html',
    styleUrls: ['./spreadsheet-search.component.scss'],
    imports: [FormsModule, MatIcon, NgClass, TranslatePipe]
})
export class SpreadsheetSearchComponent {

    /** Clé i18n du label et du placeholder du champ de recherche */
    @Input() libelle = 'searchbox.searchInData';

    /** Identifiant unique de l'input (pour l'accessibilité via <label for="...">). */
    @Input() inputId = 'spreadsheet-search-input';

    /** Émis à chaque soumission (touche Entrée) ou réinitialisation du champ. */
    @Output() searchTermsEmitter = new EventEmitter<string>();

    searchTerms = EMPTY_SEARCH;

    constructor(iconRegistryService: IconRegistryService) {
        iconRegistryService.addAllSvgIcons(ALL_TYPES);
    }

    onChanges(): void {
        this.searchTermsEmitter.emit(this.searchTerms);
    }

    onReset(): void {
        this.searchTerms = EMPTY_SEARCH;
        this.searchTermsEmitter.emit(EMPTY_SEARCH);
    }
}

