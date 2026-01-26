import {NgClass, NgIf} from '@angular/common';
import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {ExtendedModule} from '@angular/flex-layout/extended';
import {FormsModule} from '@angular/forms';
import {MatIcon, MatIconRegistry} from '@angular/material/icon';
import {DomSanitizer} from '@angular/platform-browser';
import {MediaSize} from '@core/services/breakpoint-observer.service';
import {FiltersService} from '@core/services/filters.service';
import {TranslatePipe} from '@ngx-translate/core';

const EMPTY_SEARCH = '';

@Component({
    selector: 'app-search-box',
    templateUrl: './search-box.component.html',
    styleUrls: ['./search-box.component.scss'],
    imports: [FormsModule, NgClass, ExtendedModule, MatIcon, NgIf, TranslatePipe]
})
export class SearchBoxComponent implements OnInit {
    @Input() mediaSize: MediaSize;
    @Input() libelle: string;
    @Input() submitOnReset: boolean;

    @Output() searchTermsEmitter: EventEmitter<string>;
    searchTerms = EMPTY_SEARCH;

    constructor(
        private filtersService: FiltersService,
        iconRegistry: MatIconRegistry,
        sanitizer: DomSanitizer
    ) {
        iconRegistry.addSvgIcon('filter-icon', sanitizer.bypassSecurityTrustResourceUrl('assets/icons/filter-icon.svg'));
        iconRegistry.addSvgIcon('search', sanitizer.bypassSecurityTrustResourceUrl('assets/icons/search.svg'));
        this.searchTermsEmitter = new EventEmitter<string>();
        this.mediaSize = {
            isXs: false,
            isSm: true,
            isMd: false,
            isLg: false,
            isXl: false,
            isXxl: false,
            isDeviceDesktop: true,
            isDeviceMobile: false,
        };
        this.submitOnReset = true;
    }

    onChanges(): void {
        this.searchTermsEmitter.emit(this.searchTerms);
    }

    onReset(): void {
        this.searchTerms = EMPTY_SEARCH;

        if (this.submitOnReset) {
            this.onChanges();
        }
    }

    ngOnInit(): void {
        this.searchTerms = this.filtersService.searchFilter.value;
    }
}
