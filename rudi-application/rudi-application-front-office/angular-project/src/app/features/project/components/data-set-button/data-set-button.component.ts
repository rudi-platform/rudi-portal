import {Component, Input} from '@angular/core';
import {MatButton} from '@angular/material/button';

@Component({
    selector: 'app-dataset-button',
    templateUrl: './data-set-button.component.html',
    styleUrls: ['./data-set-button.component.scss'],
    imports: [MatButton]
})
export class DataSetButtonComponent {
    @Input()
    public buttonTitle: string;
}
