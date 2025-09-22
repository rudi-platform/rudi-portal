import {Component, Input} from '@angular/core';
import {Project} from 'micro_service_modules/projekt/projekt-model';

@Component({
    selector: 'app-project-information',
    templateUrl: './project-information.component.html',
    standalone: false
})
export class ProjectInformationComponent {
    @Input() project: Project;
}
