import {Component} from '@angular/core';
import {TranslatePipe} from '@ngx-translate/core';
import {DocumentationButtonComponent} from '@shared/business/selfdata/documentation-button/documentation-button.component';
import {CardComponent} from '@shared/core/common/card/card.component';

@Component({
    selector: 'app-selfdata-dataset-basic-details',
    templateUrl: './selfdata-dataset-basic-details.component.html',
    styleUrls: ['./selfdata-dataset-basic-details.component.scss'],
    imports: [CardComponent, DocumentationButtonComponent, TranslatePipe]
})
export class SelfdataDatasetBasicDetailsComponent {

}
