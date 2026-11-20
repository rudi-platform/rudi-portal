import {HttpErrorResponse} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {TranslateService} from '@ngx-translate/core';
import {PageResultUtils} from '@shared/utils/page-result-utils';
import {TaskService as ExternalAccountRequestTaskService} from 'micro_service_modules/acl/acl-api';
import {ExternalAccountRequest} from 'micro_service_modules/acl/acl-model';
import {Form, Task} from 'micro_service_modules/api-bpmn';
import {OrganizationService} from 'micro_service_modules/strukture/api-strukture';
import {OrganizationBean} from 'micro_service_modules/strukture/strukture-model';
import {Observable, throwError} from 'rxjs';
import {catchError, switchMap} from 'rxjs/operators';

export type {ExternalAccountRequest} from 'micro_service_modules/acl/acl-model';

export interface ExternalAccountRequestWithNotifications extends ExternalAccountRequest {
    has_subscribe_to_notifications?: boolean;
}

@Injectable({
    providedIn: 'root'
})
export class ExternalAccountRequestService {

    constructor(private readonly externalAccountRequestTaskService: ExternalAccountRequestTaskService,
                private readonly organizationService: OrganizationService,
                private readonly translateService: TranslateService) {
    }

    lookupDraftForm(): Observable<Form> {
        return this.externalAccountRequestTaskService.getExternalAccountRequestDraftForm();
    }

    createDraft(asset: ExternalAccountRequest): Observable<Task> {
        return this.externalAccountRequestTaskService.createExternalAccountRequestDraft(asset);
    }

    startTask(task: Task): Observable<Task> {
        return this.externalAccountRequestTaskService.startExternalAccountRequestTask(task);
    }

    /**
     * Creates the draft from the asset then starts the workflow with the filled draft form.
     * On error, an Error with a displayable message is thrown.
     */
    submitRequest(asset: ExternalAccountRequest, draftForm: Form): Observable<Task> {
        return this.createDraft(asset).pipe(
            switchMap((task: Task) => {
                task.asset.form = draftForm;
                return this.startTask(task);
            }),
            catchError((error: HttpErrorResponse) => throwError(() => new Error(this.getErrorMessage(error))))
        );
    }

    /**
     * Public organizations the requester can declare belonging to
     */
    getOrganizations(): Observable<OrganizationBean[]> {
        return PageResultUtils.fetchAllElementsUsing(
            offset => this.organizationService.searchPublicOrganizationsBeans(null, null, null, null, false, null, offset, 100, 'name')
        );
    }

    private getErrorMessage(error: HttpErrorResponse): string {
        const errorPrefix = this.translateService.instant('externalAccountRequest.errorPrefix');
        switch (error?.status) {
            case 400:
                return errorPrefix + this.translateService.instant('externalAccountRequest.errorInvalidRequest');
            case 409:
                return errorPrefix + this.translateService.instant('externalAccountRequest.errorAlreadyRequested');
            default:
                return errorPrefix + this.translateService.instant('externalAccountRequest.errorServer');
        }
    }
}
