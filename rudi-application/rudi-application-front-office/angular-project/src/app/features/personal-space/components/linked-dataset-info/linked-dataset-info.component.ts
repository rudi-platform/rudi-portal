import {DatePipe} from '@angular/common';
import {Component, Input} from '@angular/core';
import {MatCard, MatCardContent, MatCardTitle} from '@angular/material/card';
import {TranslatePipe} from '@ngx-translate/core';
import {LoaderComponent} from '@shared/core/common/loader/loader.component';
import {LinkedDataset} from 'micro_service_modules/projekt/projekt-model';

@Component({
    selector: 'app-linked-dataset-info',
    templateUrl: './linked-dataset-info.component.html',
    styleUrls: ['./linked-dataset-info.component.scss'],
    standalone: true,
    imports: [MatCard, MatCardTitle, LoaderComponent, MatCardContent, TranslatePipe, DatePipe]
})
export class LinkedDatasetInfoComponent {
    @Input()
    linkedDataset: LinkedDataset;
    @Input()
    loading = false;
}
