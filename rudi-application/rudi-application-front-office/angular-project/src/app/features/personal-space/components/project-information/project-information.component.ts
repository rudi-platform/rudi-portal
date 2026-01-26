import {Component, Input} from '@angular/core';
import {Project} from 'micro_service_modules/projekt/projekt-model';
import {ProjectMainInformationsComponent} from '../../../project/components/project-main-informations/project-main-informations.component';

@Component({
    selector: 'app-project-information',
    templateUrl: './project-information.component.html',
    imports: [ProjectMainInformationsComponent]
})
export class ProjectInformationComponent {
    @Input() project: Project;
}
