import {Component} from '@angular/core';
import {WorkInProgressComponent} from '@shared/core/common/work-in-progress/work-in-progress.component';

@Component({
    selector: 'app-project-tasks',
    templateUrl: './project-tasks.component.html',
    imports: [WorkInProgressComponent]
})
export class ProjectTasksComponent {
}
