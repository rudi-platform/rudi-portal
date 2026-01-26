import {Component, Input} from '@angular/core';
import {MatCard, MatCardContent, MatCardSubtitle, MatCardTitle} from '@angular/material/card';
import {ClipboardFieldComponent} from '@shared/core/common/clipboard-field/clipboard-field.component';


@Component({
    selector: 'app-owner-information',
    templateUrl: './owner-information.component.html',
    styleUrls: ['./owner-information.component.scss'],
    imports: [MatCard, MatCardTitle, MatCardSubtitle, MatCardContent, ClipboardFieldComponent]
})
export class OwnerInformationComponent {

    @Input() title: string;
    @Input() name: string;
    @Input() email: string;

}
