import {Component, Input} from '@angular/core';

import {MatLabel} from '@angular/material/form-field';

@Component({
    selector: 'app-project-main-information-label',
    templateUrl: './project-main-information-label.component.html',
    styleUrls: ['./project-main-information-label.component.scss'],
    imports: [MatLabel]
})
export class ProjectMainInformationLabelComponent {
    @Input() label: string;
    @Input() value: string;
}
