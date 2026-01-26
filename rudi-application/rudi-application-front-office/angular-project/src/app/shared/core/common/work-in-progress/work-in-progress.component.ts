import {Component} from '@angular/core';
import {MatCard} from '@angular/material/card';
import {TranslateDirective} from '@ngx-translate/core';

@Component({
    selector: 'app-work-in-progress',
    templateUrl: './work-in-progress.component.html',
    styleUrls: ['./work-in-progress.component.scss'],
    imports: [MatCard, TranslateDirective]
})
export class WorkInProgressComponent {
}
