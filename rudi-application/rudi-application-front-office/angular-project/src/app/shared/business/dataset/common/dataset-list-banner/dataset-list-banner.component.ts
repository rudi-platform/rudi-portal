import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {MediaSize} from '@core/services/breakpoint-observer.service';
import {CustomizationService} from '@core/services/customization.service';
import {AccessStatusFiltersType} from '@core/services/filters/access-status-filters-type';
import {LogService} from '@core/services/log.service';
import {
    AccessStatusFilterItem
} from '@shared/business/dataset/filters/filter-forms/access-status-filter-form/access-status-filter-form.component';
import {Item} from '@shared/business/dataset/filters/filter-forms/item';
import {TranslateService} from '@ngx-translate/core';
import {PagedCmsAssets} from 'micro_service_modules/api-cms';
import {CmsAssetType} from 'micro_service_modules/api-cms/model/cmsAssetType';
import {SimpleSkosConcept} from 'micro_service_modules/kos/kos-model';
import {
    CmsDatasetsButtonDescription,
    CustomizationDescription,
    KonsultService
} from 'micro_service_modules/konsult/konsult-api';
import {MatToolbar} from '@angular/material/toolbar';
import {NgClass} from '@angular/common';
import {DomSanitizer, SafeHtml} from '@angular/platform-browser';
import {ButtonDisclosureComponent} from '@shared/core/common/button-disclosure/button-disclosure.component';
import {ThemesFilterFormComponent} from '../../filters/filter-forms/themes-filter-form/themes-filter-form.component';
import {ProducerNamesFilterFormComponent} from '../../filters/filter-forms/producer-names-filter-form/producer-names-filter-form.component';
import {AccessStatusFilterFormComponent} from '../../filters/filter-forms/access-status-filter-form/access-status-filter-form.component';

const BUTTON_OFFSET = 0;
const BUTTON_LIMIT = 1;

@Component({
    selector: 'app-dataset-list-banner',
    templateUrl: './dataset-list-banner.component.html',
    styleUrl: './dataset-list-banner.component.scss',
    imports: [
        MatToolbar,
        NgClass,
        ButtonDisclosureComponent,
        ThemesFilterFormComponent,
        ProducerNamesFilterFormComponent,
        AccessStatusFilterFormComponent,
    ],
})
export class DatasetListBannerComponent implements OnInit {
    @Input() mediaSize: MediaSize;
    @Output() selectedProducerItemsChange = new EventEmitter<Item[]>();
    @Output() selectedThemeItemsChange = new EventEmitter<Item[]>();
    @Output() selectedAccessStatusItemsChange = new EventEmitter<AccessStatusFilterItem[]>();
    @Input() themes: SimpleSkosConcept[];
    @Input() producerNames: string[];
    @Input() accessStatusForcedValue: AccessStatusFiltersType;
    @Input() accessStatusHiddenValues: AccessStatusFiltersType[];
    @Input() hideDatasetPublishButton = false;

    datasetPublishButtonHtml: SafeHtml;

    constructor(
        private readonly customizationService: CustomizationService,
        private readonly konsultService: KonsultService,
        private readonly translateService: TranslateService,
        private readonly domSanitizer: DomSanitizer,
        private readonly logger: LogService
    ) {
    }

    ngOnInit(): void {
        if (!this.hideDatasetPublishButton) {
            this.initDatasetPublishButton();
        }
    }

    private initDatasetPublishButton(): void {
        this.customizationService.getCustomizationDescription().subscribe({
            next: (customizationDescription: CustomizationDescription) => {
                const description: CmsDatasetsButtonDescription = customizationDescription?.cms_datasets_button_description;
                if (!description?.asset_type || !description?.template_simple) {
                    return;
                }
                this.loadDatasetPublishButtonAsset(description);
            },
            error: (err) => this.logger.error(err)
        });
    }

    private loadDatasetPublishButtonAsset(description: CmsDatasetsButtonDescription): void {
        const categories: string[] = description.category ? [description.category] : [];
        this.konsultService.renderAssets(
            description.asset_type as CmsAssetType,
            description.template_simple,
            categories,
            [],
            this.translateService.getCurrentLang(),
            BUTTON_OFFSET,
            BUTTON_LIMIT
        ).subscribe({
            next: (pagedCmsAssets: PagedCmsAssets) => {
                const content: string = pagedCmsAssets?.elements?.[0]?.content;
                if (pagedCmsAssets?.total > 0 && content?.trim()) {
                    this.datasetPublishButtonHtml = this.domSanitizer.bypassSecurityTrustHtml(content);
                }
            },
            error: (err) => this.logger.error(err)
        });
    }
}

