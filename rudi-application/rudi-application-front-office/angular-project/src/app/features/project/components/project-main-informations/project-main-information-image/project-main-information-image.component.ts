import {Component, Input} from '@angular/core';

import {MatLabel} from '@angular/material/form-field';

@Component({
    selector: 'app-project-main-information-image',
    templateUrl: './project-main-information-image.component.html',
    styleUrls: ['./project-main-information-image.component.scss'],
    imports: [MatLabel]
})
export class ProjectMainInformationImageComponent {
    @Input() label: string;
    @Input() src: string;
}
