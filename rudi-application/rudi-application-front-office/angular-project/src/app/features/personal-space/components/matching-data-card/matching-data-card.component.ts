import {NgFor, NgIf} from '@angular/common';
import {Component, Input} from '@angular/core';
import {SelfdataAttachmentService} from '@core/services/selfdata-attachment.service';
import {TranslatePipe} from '@ngx-translate/core';
import {CardComponent} from '@shared/core/common/card/card.component';
import {LoaderComponent} from '@shared/core/common/loader/loader.component';
import {MatchingData} from 'micro_service_modules/selfdata/selfdata-api';

@Component({
    selector: 'app-matching-data-card',
    templateUrl: './matching-data-card.component.html',
    styleUrls: ['./matching-data-card.component.scss'],
    imports: [NgIf, LoaderComponent, CardComponent, NgFor, TranslatePipe]
})
export class MatchingDataCardComponent {
    @Input() isDataTabEmpty: boolean;
    @Input() subscriptionSucced: boolean;
    @Input() data: MatchingData[];
    @Input() matchingDataLoading = false;
    title: string;

    get showMatchingDataCard(): boolean {
        return this.subscriptionSucced && !this.isDataTabEmpty;
    }

    constructor(
        private readonly selfdataAttachmentService: SelfdataAttachmentService) {
    }
}
