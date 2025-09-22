import {Component, Input} from '@angular/core';

@Component({
    selector: 'app-banner-button',
    templateUrl: './banner-button.component.html',
    styleUrls: ['./banner-button.component.scss'],
    standalone: false
})
export class BannerButtonComponent {
    @Input() projectIsUpdating;
}
