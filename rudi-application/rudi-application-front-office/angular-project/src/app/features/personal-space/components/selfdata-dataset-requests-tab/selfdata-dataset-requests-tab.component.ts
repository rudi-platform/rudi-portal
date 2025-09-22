import {Component, Input, OnInit} from '@angular/core';
import {Router} from '@angular/router';
import {SelfdataDatasetLatestRequests} from '@core/services/selfdata-dataset/selfdata-dataset-latest-requests';
import {MetadataUtils} from '@shared/utils/metadata-utils';
import {Metadata} from 'micro_service_modules/api-kaccess';
import {SelfdataInformationRequest} from 'micro_service_modules/selfdata/selfdata-model';

@Component({
    selector: 'app-selfdata-dataset-requests-tab',
    templateUrl: './selfdata-dataset-requests-tab.component.html',
    standalone: false
})
export class SelfdataDatasetRequestsTabComponent implements OnInit {

    @Input()
    metadata: Metadata;
    @Input()
    lastRequests: SelfdataDatasetLatestRequests;
    @Input()
    loading = false;
    @Input()
    loadingError = false;

    deletionSectionSubtitle: string;
    deletionSectionDescription: string;

    constructor(
        private readonly router: Router,
    ) {
    }

    get hasAccessRequest(): boolean {
        return this.lastRequests?.access != null;
    }

    get hasCorrectionRequest(): boolean {
        return this.lastRequests?.correction != null;
    }

    get hasDeletionRequest(): boolean {
        return this.lastRequests?.deletion != null;
    }

    get accessRequestFunctionalStatus(): string {
        if (this.hasAccessRequest) {
            return this.lastRequests.access.functional_status;
        }

        return null;
    }

    get correctionRequestFunctionalStatus(): string {
        if (this.hasCorrectionRequest) {
            return this.lastRequests.correction.functional_status;
        }

        return null;
    }

    get deletionRequestFunctionalStatus(): string {
        if (this.hasDeletionRequest) {
            return this.lastRequests.deletion.functional_status;
        }

        return null;
    }

    get isMetadataNonApplicableForDeletion(): boolean {
        return !MetadataUtils.isApplicableForDeletion(this.metadata);
    }

    get nonApplicableDeletionReasons(): string[] {
        return this.metadata?.ext_metadata.ext_selfdata?.ext_selfdata_content?.deletion_reason;
    }

    goToAccessRequestCreation(): void {
        const url = 'catalogue/detail/' + this.metadata?.global_id + '/selfdata-information-request-creation';
        this.router.navigate(['/' + url]);
    }

    goToRequestDetails(request: SelfdataInformationRequest): void {
        throw new Error('Page not implemented yet !');
    }

    ngOnInit(): void {
        this.setDeletableDataLabel();
    }

    private setDeletableDataLabel(): void {
        if (this.isMetadataNonApplicableForDeletion) {
            this.deletionSectionSubtitle = 'personalSpace.selfdataRequest.deletionRequest.noDeletableDataSubtitle';
        } else {
            this.deletionSectionSubtitle = 'personalSpace.selfdataRequest.deletionRequest.subTitle';
            this.deletionSectionDescription = 'personalSpace.selfdataRequest.informationRequest.text';
        }
    }
}
