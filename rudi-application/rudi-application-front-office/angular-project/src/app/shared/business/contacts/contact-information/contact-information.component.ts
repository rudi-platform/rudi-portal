import {Component, Input} from '@angular/core';
import {MatCardContent, MatCardSubtitle} from '@angular/material/card';
import {ContactButtonComponent} from '@shared/business/contacts/contact-button/contact-button.component';

@Component({
    selector: 'app-owner-contact-information',
    standalone: true,
    templateUrl: './contact-information.component.html',
    styleUrls: ['./contact-information.component.scss'],
    imports: [MatCardSubtitle, MatCardContent, ContactButtonComponent],
})
export class OwnerContactInformationComponent {
    @Input() name = '';
    @Input() email = '';
    @Input() showEmailDirectly = true;
}
