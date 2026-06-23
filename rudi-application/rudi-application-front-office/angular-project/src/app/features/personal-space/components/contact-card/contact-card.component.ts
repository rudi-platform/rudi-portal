import {Component, Input} from '@angular/core';
import {MatCard, MatCardTitle} from '@angular/material/card';
import {ContactInformationComponent} from '../contact-information/contact-information.component';

export interface RelatedOrganizationInfo {
    name: string;
    adminEmail?: string;
}

@Component({
    selector: 'app-contact-card',
    standalone: true,
    templateUrl: './contact-card.component.html',
    styleUrls: ['./contact-card.component.scss'],
    imports: [MatCard, MatCardTitle, ContactInformationComponent]
})
export class ContactCardComponent {

    @Input() title = '';
    @Input() name = '';
    @Input() email = '';

    @Input() relatedTitle = '';
    @Input() relatedOrganizations: RelatedOrganizationInfo[] = [];

}
