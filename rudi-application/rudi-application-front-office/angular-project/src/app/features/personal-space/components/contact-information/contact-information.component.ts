import {Component, Input} from '@angular/core';
import {MatCardContent, MatCardSubtitle} from '@angular/material/card';
import {SharedModule} from '@shared/shared.module';

@Component({
    selector: 'app-contact-information',
    standalone: true,
    templateUrl: './contact-information.component.html',
    styleUrls: ['./contact-information.component.scss'],
    imports: [MatCardSubtitle, MatCardContent, SharedModule],
})
export class ContactInformationComponent {
    @Input() name = '';
    @Input() email = '';
}
