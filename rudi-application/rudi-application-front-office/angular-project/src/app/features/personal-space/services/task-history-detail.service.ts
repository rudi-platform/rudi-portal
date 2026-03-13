import {Injectable} from '@angular/core';
import {concat, Observable, of} from 'rxjs';
import {catchError, map, take, filter, defaultIfEmpty} from 'rxjs/operators';
import {ProcessHistoricInformation} from 'micro_service_modules/api-bpmn';
import {TaskService as ProjektTaskService} from 'micro_service_modules/projekt/projekt-api';
import {TaskService as StruktureTaskService} from 'micro_service_modules/strukture/api-strukture';
import {TaskService as SelfdataTaskService} from 'micro_service_modules/selfdata/selfdata-api';

@Injectable({
    providedIn: 'root'
})
export class TaskHistoryDetailService {

    constructor(
        private readonly projektTaskService: ProjektTaskService,
        private readonly struktureTaskService: StruktureTaskService,
        private readonly selfdataTaskService: SelfdataTaskService,
    ) {
    }

    findHistoricAcrossServices(historicId: string): Observable<ProcessHistoricInformation | null> {
        // Prefer "detail-by-id" endpoints where they exist.
        const detailCalls: Observable<ProcessHistoricInformation | null>[] = [
            this.safe(this.projektTaskService.getProjectTaskHistoryByTaskId(historicId)),
            this.safe(this.projektTaskService.getLinkedDatasetTaskHistoryByTaskId(historicId)),
            this.safe(this.projektTaskService.getNewDatasetRequestTaskHistoryByTaskId(historicId)),
            this.safe(this.struktureTaskService.getOrganizationTaskHistoryByTaskId(historicId)),
            this.safe(this.struktureTaskService.getLinkedProducerTaskHistoryByTaskId(historicId)),
        ];

        // Selfdata has no task-history-by-taskId endpoint in the generated client.
        const listFallbackCalls: Observable<ProcessHistoricInformation | null>[] = [
            this.findInSelfdataHistorics(historicId),
            this.findInProjektHistorics(historicId),
            this.findInStruktureHistorics(historicId),
        ];

        return concat(...detailCalls, ...listFallbackCalls).pipe(
            filter((value): value is ProcessHistoricInformation => value != null),
            take(1),
            defaultIfEmpty(null),
        );
    }

    private findInSelfdataHistorics(historicId: string): Observable<ProcessHistoricInformation | null> {
        return this.selfdataTaskService.getMyHistoricInformations().pipe(
            catchError(() => of([])),
            map(list => list?.find(item => item?.id === historicId) ?? null),
        );
    }

    private findInProjektHistorics(historicId: string): Observable<ProcessHistoricInformation | null> {
        return this.projektTaskService.getMyHistoricInformations().pipe(
            catchError(() => of([])),
            map(list => list?.find(item => item?.id === historicId) ?? null),
        );
    }

    private findInStruktureHistorics(historicId: string): Observable<ProcessHistoricInformation | null> {
        return this.struktureTaskService.getMyHistoricInformations().pipe(
            catchError(() => of([])),
            map(list => list?.find(item => item?.id === historicId) ?? null),
        );
    }

    private safe(source$: Observable<ProcessHistoricInformation>): Observable<ProcessHistoricInformation | null> {
        return source$.pipe(catchError(() => of(null)));
    }
}
