import {Component, Input, OnInit} from '@angular/core';
import {ProcessHistoricInformation} from 'micro_service_modules/api-bpmn';
import {TaskService} from 'micro_service_modules/projekt/projekt-api';
import {Observable} from 'rxjs';
import {NgIf, NgFor, AsyncPipe, DatePipe} from '@angular/common';
import {TranslatePipe} from '@ngx-translate/core';

@Component({
    selector: 'app-project-task-historic',
    templateUrl: './project-task-historic.component.html',
    styleUrls: ['./project-task-historic.component.scss'],
    imports: [NgIf, NgFor, AsyncPipe, DatePipe, TranslatePipe]
})
export class ProjectTaskHistoricComponent implements OnInit {

    @Input()
    taskId: string;

    processHistoricInformation$: Observable<ProcessHistoricInformation>;

    constructor(private readonly taskService: TaskService) {
    }

    ngOnInit(): void {
        this.processHistoricInformation$ = this.taskService.getProjectTaskHistoryByTaskId(this.taskId, false);
    }
}
