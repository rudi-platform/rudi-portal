import {Injectable} from '@angular/core';
import {Status} from 'micro_service_modules/api-bpmn';
import {Project} from 'micro_service_modules/projekt/projekt-model';

@Injectable({
    providedIn: 'root'
})
export class DataSetActionsAuthorizationService {

    canAddDatasetFromProjectFromDetail(project: Project): boolean {
        return this.handleActionAuthorization(project, false);
    }

    canDeleteDatasetFromProjectFromDetail(project: Project): boolean {
        return this.handleActionAuthorization(project, false);
    }

    canAddDatasetFromProjectFromTask(project: Project): boolean {
        return this.handleActionAuthorization(project, true);
    }

    canDeleteDatasetFromProjectFromTask(project: Project): boolean {
        return this.handleActionAuthorization(project, true);
    }

    private handleActionAuthorization(project: Project, fromTask: boolean): boolean {
        switch (project.project_status) {
            case 'DRAFT':
            case 'REJECTED':
                return fromTask; // modification des jdd ou new dataset requests autorisée uniquement depuis une tâche sur une réutilisation pas encore finalisée
            case 'VALIDATED' :
                return project.reutilisation_status.dataset_set_modification_allowed && project.status === Status.Completed;
            case 'IN_PROGRESS':
            case 'CANCELLED':
            case 'DISENGAGED':
            default:
                return false;
        }
    }

}
