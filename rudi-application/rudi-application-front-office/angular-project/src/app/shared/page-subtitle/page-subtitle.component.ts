import {Component, Input} from '@angular/core';

@Component({
    selector: 'app-page-subtitle',
    templateUrl: './page-subtitle.component.html',
    styleUrls: ['./page-subtitle.component.scss'],
    standalone: false
})
export class PageSubtitleComponent {

    @Input()
    text: string;
}
