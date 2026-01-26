import {NgFor, NgIf} from '@angular/common';
import {Component, Input} from '@angular/core';
import {LanguageService} from '@core/i18n/language.service';
import {GdataDataInterface} from '@core/services/selfdata-dataset/gdataData.interface';
import {CardComponent} from '@shared/core/common/card/card.component';
import {LoaderComponent} from '@shared/core/common/loader/loader.component';
import {DictionaryEntry} from 'micro_service_modules/api-kaccess';

@Component({
    selector: 'app-generic-data',
    templateUrl: './generic-data.component.html',
    styleUrls: ['./generic-data.component.scss'],
    imports: [CardComponent, LoaderComponent, NgIf, NgFor]
})
export class GenericDataComponent {
    @Input() isLoading: boolean;
    @Input() genericDataObject: GdataDataInterface;

    constructor(
        private readonly languageService: LanguageService) {
    }

    getLabel(dictionaryEntries: DictionaryEntry[]): string {
        return this.languageService.getTextForCurrentLanguage(dictionaryEntries);
    }
}
