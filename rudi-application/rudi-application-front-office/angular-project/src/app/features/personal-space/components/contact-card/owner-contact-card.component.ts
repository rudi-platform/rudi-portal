import {Component, Input} from '@angular/core';
import {MatCard} from '@angular/material/card';
import {OwnerCardInfoComponent, RelatedOrganizationInfo} from '@shared/business/contacts/owner-card-info/owner-card-info.component';

@Component({
    selector: 'app-owner-contact-card',
    standalone: true,
    templateUrl: './owner-contact-card.component.html',
    styleUrls: ['./owner-contact-card.component.scss'],
    imports: [MatCard, OwnerCardInfoComponent]
})
export class OwnerContactCardComponent {

    @Input() title = '';
    @Input() name = '';
    @Input() email = '';

    @Input() relatedTitle = '';
    @Input() relatedOrganizations: RelatedOrganizationInfo[] = [];

}
