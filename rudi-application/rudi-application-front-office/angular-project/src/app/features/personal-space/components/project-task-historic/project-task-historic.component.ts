import {Component, Input, OnInit} from '@angular/core';
import {HistoricInformation, TaskService} from 'micro_service_modules/projekt/projekt-api';
import {Observable} from 'rxjs';

@Component({
    selector: 'app-project-task-historic',
    templateUrl: './project-task-historic.component.html',
    styleUrls: ['./project-task-historic.component.scss']
})
export class ProjectTaskHistoricComponent implements OnInit {

    @Input()
    taskId: string;

    historic$: Observable<Array<HistoricInformation>>;

    constructor(private readonly taskService: TaskService) {
    }

    ngOnInit(): void {
        this.historic$ = this.taskService.getProjectTaskHistoryByTaskId(this.taskId, false);
    }
}
