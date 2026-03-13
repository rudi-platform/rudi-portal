import {Component, Input} from '@angular/core';
import {MatCard, MatCardContent, MatCardSubtitle, MatCardTitle} from '@angular/material/card';
import {SharedModule} from '@shared/shared.module';


@Component({
    selector: 'app-owner-information',
    standalone: true,
    templateUrl: './owner-information.component.html',
    styleUrls: ['./owner-information.component.scss'],
    imports: [MatCard, MatCardTitle, MatCardSubtitle, MatCardContent, SharedModule]
})
export class OwnerInformationComponent {

    @Input() title = '';
    @Input() name = '';
    @Input() email = '';

}
