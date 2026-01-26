import {NgIf} from '@angular/common';
import {Component, OnInit} from '@angular/core';
import {ActivatedRoute} from '@angular/router';
import {IconRegistryService} from '@core/services/icon-registry.service';
import {KonsultMetierService} from '@core/services/konsult-metier.service';
import {PageTitleService} from '@core/services/page-title.service';
import {SelfdataDatasetLatestRequests} from '@core/services/selfdata-dataset/selfdata-dataset-latest-requests';
import {SelfdataDatasetService} from '@core/services/selfdata-dataset/selfdata-dataset.service';
import {TranslatePipe, TranslateService} from '@ngx-translate/core';
import {LoaderComponent} from '@shared/core/common/loader/loader.component';
import {TabComponent} from '@shared/core/common/tab/tab.component';
import {TabsComponent} from '@shared/core/common/tabs/tabs.component';
import {PageHeadingComponent} from '@shared/core/layout/page-heading/page-heading.component';
import {PageComponent} from '@shared/core/layout/page/page.component';
import {ALL_TYPES} from '@shared/models/title-icon-type';
import {TabContentDirective} from '@shared/utils/directives/tab-content-directive/tab-content.directive';
import {TabsLayoutDirective} from '@shared/utils/directives/tab-layout-directive/tabs-layout.directive';
import {MetadataUtils} from '@shared/utils/metadata-utils';
import {Metadata} from 'micro_service_modules/api-kaccess';
import {MatchingData} from 'micro_service_modules/selfdata/selfdata-api';
import {Observable, throwError} from 'rxjs';
import {switchMap} from 'rxjs/operators';
import {MatchingDataCardComponent} from '../../components/matching-data-card/matching-data-card.component';
import {MatchingDataView} from '../../components/matching-data-card/matching-data-view';
import {
    SelfdataDatasetBasicDetailsComponent
} from '../../components/selfdata-dataset-basic-details/selfdata-dataset-basic-details.component';
import {SelfdataDatasetDataTabComponent} from '../../components/selfdata-dataset-data-tab/selfdata-dataset-data-tab.component';
import {SelfdataDatasetRequestsTabComponent} from '../../components/selfdata-dataset-requests-tab/selfdata-dataset-requests-tab.component';

@Component({
    selector: 'app-selfdata-dataset-details',
    templateUrl: './selfdata-dataset-details.component.html',
    styleUrls: ['./selfdata-dataset-details.component.scss'],
    imports: [PageComponent, LoaderComponent, NgIf, PageHeadingComponent, TabsComponent, TabComponent, SelfdataDatasetRequestsTabComponent, SelfdataDatasetDataTabComponent, TabsLayoutDirective, TabContentDirective, MatchingDataCardComponent, SelfdataDatasetBasicDetailsComponent, TranslatePipe]
})
export class SelfdataDatasetDetailsComponent implements OnInit {

    metadata: Metadata;
    dataLoading = false;
    errorLoading = false;
    lastRequests: SelfdataDatasetLatestRequests;
    datasetUuid: string;
    matchingDataLoading = false;
    matchingData: MatchingData[] = [];
    subscriptionSucced = false;


    constructor(
        private readonly iconRegistryService: IconRegistryService,
        private readonly route: ActivatedRoute,
        private readonly konsultMetierService: KonsultMetierService,
        private readonly selfdataDatasetService: SelfdataDatasetService,
        private readonly pageTitleService: PageTitleService,
        private readonly translateService: TranslateService
    ) {
        iconRegistryService.addAllSvgIcons(ALL_TYPES);
    }

    ngOnInit(): void {
        this.dataLoading = true;
        this.errorLoading = false;
        this.route.params.pipe(
            switchMap(params => this.konsultMetierService.getMetadataByUuid(params.datasetUuid)),
            switchMap(metadata => {
                this.pageTitleService.setPageTitle(metadata.resource_title, this.translateService.instant('pageTitle.defaultDetail'));
                this.metadata = metadata;
                this.getMySelfdataInformationRequestMatchingData(this.metadata.global_id);
                return this.getLatestRequests(this.metadata.global_id);
            })
        ).subscribe({
            next: (latestRequests: SelfdataDatasetLatestRequests) => {
                this.lastRequests = latestRequests;
                this.dataLoading = false;
                this.errorLoading = false;
            },
            error: (e) => {
                console.error(e);
                this.dataLoading = false;
                this.errorLoading = true;
            }
        });
    }

    get isDataTabEmpty(): boolean {
        return this.selfdataDatasetService.isDataTabEmpty(this.lastRequests);
    }

    /**
     * méthode qui récupère les dernières demandes d'un utilisateur sur un jdd donné
     * @param datasetUuid
     */
    getLatestRequests(datasetUuid: string): Observable<SelfdataDatasetLatestRequests> {
        this.dataLoading = true;
        this.errorLoading = false;
        return this.konsultMetierService.getMetadataByUuid(datasetUuid).pipe(
            switchMap((metadata: Metadata) => {
                if (metadata) {
                    this.metadata = metadata;
                    return this.selfdataDatasetService.searchAllMyLatestRequests(metadata);
                }
                return throwError(() => 'Aucun metadata trouvé');
            })
        );
    }

    hasDataTab(): boolean {
        return MetadataUtils.isSelfdataAccessApi(this.metadata);
    }

    /**
     * getMySelfdataInformationRequestMatchingData , méthode qui réccupere les matchingData
     * @param datasetUuid
     */
    getMySelfdataInformationRequestMatchingData(datasetUuid: string): void {
        this.matchingDataLoading = true;
        this.selfdataDatasetService.getMySelfdataInformationRequestMatchingData(datasetUuid).subscribe({
            next: (data: MatchingDataView[]) => {
                this.matchingData = data;
                this.matchingDataLoading = false;
            },
            error: (e) => {
                console.error(e);
                this.matchingDataLoading = false;
            }
        });
    }

    /**
     * Méthode qui récupère le subscriptionSucced
     */
    handleSubscriptionSuccedChanged(subscriptionSucced: boolean): void {
        this.subscriptionSucced = subscriptionSucced;
    }
}
