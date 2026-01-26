import {Component, Input} from '@angular/core';
import {NgIf, DatePipe} from '@angular/common';
import {ProjectMainInformationLabelComponent} from '../project-main-information-label/project-main-information-label.component';
import {TranslatePipe} from '@ngx-translate/core';

@Component({
    selector: 'app-project-main-information-date',
    templateUrl: './project-main-information-date.component.html',
    imports: [NgIf, ProjectMainInformationLabelComponent, DatePipe, TranslatePipe]
})
export class ProjectMainInformationDateComponent {
    @Input() startDate: string = null;
    @Input() endDate: string = null;
}
