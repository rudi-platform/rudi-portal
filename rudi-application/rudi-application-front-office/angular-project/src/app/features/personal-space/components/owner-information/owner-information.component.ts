import {Component, Input} from '@angular/core';


@Component({
    selector: 'app-owner-information',
    templateUrl: './owner-information.component.html',
    styleUrls: ['./owner-information.component.scss']
})
export class OwnerInformationComponent {

    @Input() title: string;
    @Input() name: string;
    @Input() email: string;

}
