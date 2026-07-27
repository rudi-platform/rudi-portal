import {Component, Input} from '@angular/core';
import {OwnerContactInformationComponent} from '@shared/business/contacts/contact-information/contact-information.component';

export interface RelatedOrganizationInfo {
    name: string;
    adminEmail?: string;
}

@Component({
    selector: 'app-owner-card-info',
    standalone: true,
    templateUrl: './owner-card-info.component.html',
    styleUrls: ['./owner-card-info.component.scss'],
    imports: [OwnerContactInformationComponent]
})
export class OwnerCardInfoComponent {

    @Input() title = '';
    @Input() name = '';
    @Input() email = '';

    @Input() relatedTitle = '';
    @Input() relatedOrganizations: RelatedOrganizationInfo[] = [];

    @Input() showOwnerEmailDirectly = true;
    @Input() showRelatedEmailDirectly = false;

}
