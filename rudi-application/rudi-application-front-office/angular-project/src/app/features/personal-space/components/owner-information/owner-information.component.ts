import {Component, Input} from '@angular/core';


@Component({
    selector: 'app-owner-information',
    templateUrl: './owner-information.component.html',
    styleUrls: ['./owner-information.component.scss'],
    standalone: false
})
export class OwnerInformationComponent {

    @Input() title: string;
    @Input() name: string;
    @Input() email: string;

}
