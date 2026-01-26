import {Component} from '@angular/core';
import {MyLinkedDatasetsComponent} from '../my-requests-tables/my-linked-datasets/my-linked-datasets.component';
import {MySelfdataRequestsComponent} from '../my-requests-tables/my-selfdata-requests/my-selfdata-requests.component';
import {MyNewDatasetRequestsComponent} from '../my-requests-tables/my-new-dataset-requests/my-new-dataset-requests.component';

@Component({
    selector: 'app-my-requests',
    templateUrl: './my-requests.component.html',
    styleUrls: ['./my-requests.component.scss'],
    imports: [MyLinkedDatasetsComponent, MySelfdataRequestsComponent, MyNewDatasetRequestsComponent]
})
export class MyRequestsComponent {
}
