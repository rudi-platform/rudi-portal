import {CommonModule} from '@angular/common';
import {CUSTOM_ELEMENTS_SCHEMA, NgModule} from '@angular/core';
import {MatStepperModule} from '@angular/material/stepper';
import {CoreModule} from '@core/core.module';
import {
    ProjectMainInformationDateComponent
} from '@features/project/components/project-main-informations/project-main-information-date/project-main-information-date.component';
import {
    ProjectMainInformationLabelComponent
} from '@features/project/components/project-main-informations/project-main-information-label/project-main-information-label.component';
import {ProjectMainInformationsComponent} from '@features/project/components/project-main-informations/project-main-informations.component';
import {SharedModule} from '@shared/shared.module';
import {FilePickerModule} from '@sleiss/ngx-awesome-uploader';
import {BannerComponent} from './components/banner/banner.component';
import {DataSetButtonComponent} from './components/data-set-button/data-set-button.component';
import {EditNewDataSetDialogComponent} from './components/edit-new-data-set-dialog/edit-new-data-set-dialog.component';
import {OrderComponent} from './components/order/order.component';
import {ProjectDatasetListComponent} from './components/project-dataset-list/project-dataset-list.component';
import {
    ProjectMainInformationRichLabelComponent
} from './components/project-main-informations/project-main-information-rich-label/project-main-information-rich-label.component';
import {RequestDetailsDialogComponent} from './components/request-details-dialog/request-details-dialog.component';
import {ReuseProjectCommonComponent} from './components/reuse-project-common/reuse-project-common.component';
import {Step1ProjectComponent} from './components/step1-project/step1-project.component';
import {Step2ProjectComponent} from './components/step2-project/step2-project.component';
import {Step3ProjectComponent} from './components/step3-project/step3-project.component';
import {
    SuccessProjectCreationDialogComponent
} from './components/success-project-creation-dialog/success-project-creation-dialog.component';
import {SuccessStep3TemplateComponent} from './components/success-step3-template/success-step3-template.component';
import {DetailComponent} from './pages/detail/detail.component';
import {ListComponent} from './pages/list/list.component';
import {SubmissionProjectComponent} from './pages/submission-project/submission-project.component';
import {ProjectRoutingModule} from './project-routing.module';

@NgModule({
    imports: [
        CommonModule,
        SharedModule,
        CoreModule,
        FilePickerModule,
        ProjectRoutingModule,
        MatStepperModule,
        ListComponent,
        BannerComponent,
        DetailComponent,
        OrderComponent,
        SubmissionProjectComponent,
        DataSetButtonComponent,
        EditNewDataSetDialogComponent,
        SubmissionProjectComponent,
        Step1ProjectComponent,
        Step2ProjectComponent,
        Step3ProjectComponent,
        ReuseProjectCommonComponent,
        SuccessProjectCreationDialogComponent,
        ProjectDatasetListComponent,
        RequestDetailsDialogComponent,
        SuccessStep3TemplateComponent,
        ProjectMainInformationsComponent,
        ProjectMainInformationDateComponent,
        ProjectMainInformationLabelComponent,
        ProjectMainInformationRichLabelComponent
    ],
    exports: [ProjectMainInformationsComponent, ProjectMainInformationLabelComponent, ProjectMainInformationDateComponent,],
    providers: [
        {provide: 'DEFAULT_LANGUAGE', useValue: 'fr'}
    ],
    schemas: [CUSTOM_ELEMENTS_SCHEMA]
})

export class ProjectModule {
}
