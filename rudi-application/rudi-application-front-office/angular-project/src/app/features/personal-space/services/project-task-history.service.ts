import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {TaskService as ProjektTaskService} from 'micro_service_modules/projekt/projekt-api';
import {ProcessHistoricInformation} from 'micro_service_modules/api-bpmn';

@Injectable({
    providedIn: 'root'
})
export class ProjectTaskHistoryService {

    constructor(private readonly projektTaskService: ProjektTaskService) {
    }

    getProjectTaskHistoryByTaskId(taskId: string): Observable<ProcessHistoricInformation> {
        return this.projektTaskService.getProjectTaskHistoryByTaskId(taskId);
    }
}
