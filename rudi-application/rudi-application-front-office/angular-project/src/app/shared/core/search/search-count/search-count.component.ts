import {Component, Input, OnInit} from '@angular/core';
import {RouterLink} from '@angular/router';
import {TranslatePipe, TranslateService} from '@ngx-translate/core';
import {CustomRouterlinkDirective} from '../../../utils/directives/custom-routerlink-directive/custom-routerlink.directive';

const DEFAULT_TEXT_RESULT = 'searchbox.results';

@Component({
    selector: 'app-search-count',
    templateUrl: './search-count.component.html',
    styleUrls: ['./search-count.component.scss'],
    imports: [RouterLink, CustomRouterlinkDirective, TranslatePipe],
    host: {
        '[attr.role]': 'headingLevel ? "heading" : null',
        '[attr.aria-level]': 'headingLevel ?? null'
    }
})
export class SearchCountComponent implements OnInit {

    @Input()
    searchIsRunning = true;

    @Input()
    count = 0;

    @Input()
    noResultMessage: string;

    // Not named routerLink: the RouterLink directive of the parent would make the whole host clickable
    @Input()
    linkUrl: string;

    @Input()
    hyperLink: string;

    @Input()
    resultMessage = DEFAULT_TEXT_RESULT;

    // Exposes the count as a section heading without changing its look
    @Input()
    headingLevel: number;

    constructor(
        private readonly translateService: TranslateService,
    ) {
    }

    ngOnInit(): void {
        if (!this.noResultMessage) {
            this.translateService.get('searchbox.noResult').subscribe(noResultMessage => this.noResultMessage = noResultMessage);
        }
    }

    get isDefaultText(): boolean {
        return this.resultMessage === DEFAULT_TEXT_RESULT;
    }

}
