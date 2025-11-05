import {CommonModule} from '@angular/common';
import {CUSTOM_ELEMENTS_SCHEMA, NgModule} from '@angular/core';
import {CoreModule} from '@core/core.module';
import {SharedModule} from '@shared/shared.module';
import {GetBackendPropertyPipe} from '@shared/utils/pipes/get-backend-property.pipe';
import {AgGridModule} from 'ag-grid-angular';
import {DataSetInfosComponent} from './components/data-set-infos/data-set-infos.component';
import {DatasetInformationsComponent} from './components/dataset-informations/dataset-informations.component';
import {MapTabComponent} from './components/map-tab/map-tab.component';
import {SelectProjectDialogComponent} from './components/select-project-dialog/select-project-dialog.component';
import {SpreadsheetTabComponent} from './components/spreadsheet-tab/spreadsheet-tab.component';
import {SpreadsheetComponent} from './components/spreadsheet/spreadsheet.component';
import {
    SuccessRestrictedRequestDialogComponent
} from './components/success-restricted-request-dialog/success-restricted-request-dialog.component';
import {DataSetRoutingModule} from './data-set-routing.module';
import {DetailComponent} from './pages/detail/detail.component';
import {ListComponent} from './pages/list/list.component';
import {
    SelfdataInformationRequestCreationSuccessComponent
} from './pages/selfdata-information-request-creation-success/selfdata-information-request-creation-success.component';
import {
    SelfdataInformationRequestCreationComponent
} from './pages/selfdata-information-request-creation/selfdata-information-request-creation.component';


@NgModule({
    declarations:
        [
            DataSetInfosComponent,
            DetailComponent,
            ListComponent,
            SelectProjectDialogComponent,
            SuccessRestrictedRequestDialogComponent,
            SelfdataInformationRequestCreationComponent,
            SelfdataInformationRequestCreationSuccessComponent,
            SpreadsheetComponent,
            DatasetInformationsComponent,
            SpreadsheetTabComponent,
            MapTabComponent
        ],
    imports: [
        CommonModule,
        CoreModule,
        SharedModule,
        DataSetRoutingModule,
        AgGridModule
    ],
    exports: [
        SelectProjectDialogComponent
    ],
    schemas: [CUSTOM_ELEMENTS_SCHEMA],
    providers: [
        {provide: 'DEFAULT_LANGUAGE', useValue: 'fr'},
        GetBackendPropertyPipe
    ]
})
export class DataSetModule {
}
