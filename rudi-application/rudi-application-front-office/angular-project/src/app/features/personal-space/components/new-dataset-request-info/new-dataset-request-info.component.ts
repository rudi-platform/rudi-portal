import {DatePipe} from '@angular/common';
import {Component, Input} from '@angular/core';
import {MatCard, MatCardContent, MatCardTitle} from '@angular/material/card';
import {TranslatePipe} from '@ngx-translate/core';
import {LoaderComponent} from '@shared/core/common/loader/loader.component';
import {NewDatasetRequest} from 'micro_service_modules/projekt/projekt-model';

@Component({
    selector: 'app-new-dataset-request-info',
    templateUrl: './new-dataset-request-info.component.html',
    styleUrls: ['./new-dataset-request-info.component.scss'],
    standalone: true,
    imports: [MatCard, MatCardTitle, LoaderComponent, MatCardContent, TranslatePipe, DatePipe]
})
export class NewDatasetRequestInfoComponent {
    @Input()
    newDatasetRequest: NewDatasetRequest;
    @Input()
    loading = false;
}
