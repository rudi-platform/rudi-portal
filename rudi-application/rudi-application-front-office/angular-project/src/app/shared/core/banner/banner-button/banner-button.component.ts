import {Component, Input} from '@angular/core';
import {MatButton} from '@angular/material/button';

@Component({
    selector: 'app-banner-button',
    templateUrl: './banner-button.component.html',
    styleUrls: ['./banner-button.component.scss'],
    imports: [MatButton]
})
export class BannerButtonComponent {
    @Input() projectIsUpdating;
}
