import {CommonModule} from '@angular/common';
import {provideHttpClient} from '@angular/common/http';
import {CUSTOM_ELEMENTS_SCHEMA, NgModule} from '@angular/core';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {MatAutocompleteModule} from '@angular/material/autocomplete';
import {MatTableModule} from '@angular/material/table';
import {CoreModule} from '@core/core.module';
import {NgbPopoverModule} from '@ng-bootstrap/ng-bootstrap';
import {ContactButtonComponent} from '@shared/business/contacts/contact-button/contact-button.component';
import {ContactCardComponent} from '@shared/business/contacts/contact-card/contact-card.component';
import {DataSetCardComponent} from '@shared/business/dataset/common/data-set-card/data-set-card.component';
import {DatasetsInfosComponent} from '@shared/business/dataset/common/dataset-infos/dataset-infos.component';
import {DatasetListBannerComponent} from '@shared/business/dataset/common/dataset-list-banner/dataset-list-banner.component';
import {DatasetListComponent} from '@shared/business/dataset/common/dataset-list/dataset-list.component';
import {
    AccessStatusFilterFormComponent
} from '@shared/business/dataset/filters/filter-forms/access-status-filter-form/access-status-filter-form.component';
import {DatesFilterFormComponent} from '@shared/business/dataset/filters/filter-forms/dates-filter-form/dates-filter-form.component';
import {OrderFilterFormComponent} from '@shared/business/dataset/filters/filter-forms/order-filter-form/order-filter-form.component';
import {
    ProducerNamesFilterFormComponent
} from '@shared/business/dataset/filters/filter-forms/producer-names-filter-form/producer-names-filter-form.component';
import {ThemesFilterFormComponent} from '@shared/business/dataset/filters/filter-forms/themes-filter-form/themes-filter-form.component';
import {FilterMenuComponent} from '@shared/business/dataset/filters/filter-menu/filter-menu.component';
import {
    FilterSidenavContainerComponent
} from '@shared/business/dataset/filters/filter-sidenav-container/filter-sidenav-container.component';
import {FiltersItemsListComponent} from '@shared/business/dataset/filters/filters-items-list/filters-items-list.component';
import {ListContainerComponent} from '@shared/business/dataset/filters/list-container/list-container.component';
import {OrderComponent} from '@shared/business/dataset/filters/order/order.component';
import {RudiSwiperComponent} from '@shared/business/home/rudi-swiper/rudi-swiper.component';
import {SocialMediaSectionComponent} from '@shared/business/home/social-media-section/social-media-section.component';
import {ListOrganizationCardComponent} from '@shared/business/organisation/list-organization-card/list-organization-card.component';
import {MemberPopinComponent} from '@shared/business/organisation/member-popin/member-popin.component';
import {OrganizationCardComponent} from '@shared/business/organisation/organization-card/organization-card.component';
import {OrganizationLogoComponent} from '@shared/business/organisation/organization-logo/organization-logo.component';
import {ProjectCardComponent} from '@shared/business/projects/project-card/project-card.component';
import {ProjectHeadingComponent} from '@shared/business/projects/project-heading/project-heading.component';
import {ProjectListComponent} from '@shared/business/projects/project-list/project-list.component';
import {DatasetTableComponent} from '@shared/business/projects/projects-datasets-tables/dataset-table/dataset-table.component';
import {
    DeletionConfirmationPopinComponent
} from '@shared/business/projects/projects-datasets-tables/deletion-confirmation-popin/deletion-confirmation-popin.component';
import {
    NewDatasetRequestTableComponent
} from '@shared/business/projects/projects-datasets-tables/new-dataset-request-table/new-dataset-request-table.component';
import {
    OpenDatasetTableComponent
} from '@shared/business/projects/projects-datasets-tables/open-dataset-table/open-dataset-table.component';
import {
    RestrictedDatasetTableComponent
} from '@shared/business/projects/projects-datasets-tables/restricted-dataset-table/restricted-dataset-table.component';
import {DocumentationButtonComponent} from '@shared/business/selfdata/documentation-button/documentation-button.component';
import {BannerButtonComponent} from '@shared/core/banner/banner-button/banner-button.component';
import {BannerComponent} from '@shared/core/banner/banner/banner.component';
import {BackPaginationComponent} from '@shared/core/common/back-pagination/back-pagination.component';
import {BooleanDataBlockComponent} from '@shared/core/common/boolean-data-block/boolean-data-block.component';
import {CardComponent} from '@shared/core/common/card/card.component';
import {ClipboardFieldComponent} from '@shared/core/common/clipboard-field/clipboard-field.component';
import {CopiedButtonComponent} from '@shared/core/common/copied-button/copied-button.component';
import {ErrorBoxComponent} from '@shared/core/common/error-box/error-box.component';
import {ErrorPageComponent} from '@shared/core/common/error-page/error-page.component';
import {LabelSeparatorComponent} from '@shared/core/common/label-separator/label-separator.component';
import {LoaderComponent} from '@shared/core/common/loader/loader.component';
import {PaginatorComponent} from '@shared/core/common/paginator/paginator.component';
import {TabComponent} from '@shared/core/common/tab/tab.component';
import {TabsComponent} from '@shared/core/common/tabs/tabs.component';
import {WorkInProgressComponent} from '@shared/core/common/work-in-progress/work-in-progress.component';
import {MonthYearDatepickerComponent} from '@shared/core/form/month-year-datepicker/month-year-datepicker.component';
import {PasswordStrengthComponent} from '@shared/core/form/password-strength/password-strength.component';
import {PasswordComponent} from '@shared/core/form/password/password.component';
import {RadioListComponent} from '@shared/core/form/radio-list/radio-list.component';
import {ResetPasswordErrorBoxComponent} from '@shared/core/form/reset-password-error-box/reset-password-error-box.component';
import {RudiCaptchaComponent} from '@shared/core/form/rudi-captcha/rudi-captcha.component';
import {UploaderComponent} from '@shared/core/form/uploader/uploader.component';
import {FooterComponent} from '@shared/core/layout/footer/footer.component';
import {HeaderComponent} from '@shared/core/layout/header/header.component';
import {NotificationTemplateComponent} from '@shared/core/layout/notification-template/notification-template.component';
import {PageHeadingComponent} from '@shared/core/layout/page-heading/page-heading.component';
import {PageSubtitleComponent} from '@shared/core/layout/page-subtitle/page-subtitle.component';
import {PageTitleComponent} from '@shared/core/layout/page-title/page-title.component';
import {PageComponent} from '@shared/core/layout/page/page.component';
import {PopoverComponent} from '@shared/core/layout/popover/popover.component';
import {MapPopupComponent} from '@shared/core/maps/map-popup/map-popup.component';
import {MapComponent} from '@shared/core/maps/map/map.component';
import {SearchAutocompleteComponent} from '@shared/core/search/search-autocomplete/search-autocomplete.component';
import {SearchBoxComponent} from '@shared/core/search/search-box/search-box.component';
import {SearchCountComponent} from '@shared/core/search/search-count/search-count.component';
import {TaskDetailHeaderComponent} from '@shared/core/workflow/common/task-detail-header/task-detail-header.component';
import {WorkflowFieldAddressComponent} from '@shared/core/workflow/fields/workflow-field-address/workflow-field-address.component';
import {
    WorkflowFieldAttachmentPopinComponent
} from '@shared/core/workflow/fields/workflow-field-attachment-popin/workflow-field-attachment-popin.component';
import {WorkflowFieldAttachmentComponent} from '@shared/core/workflow/fields/workflow-field-attachment/workflow-field-attachment.component';
import {WorkflowFieldBooleanComponent} from '@shared/core/workflow/fields/workflow-field-boolean/workflow-field-boolean.component';
import {WorkflowFieldDateComponent} from '@shared/core/workflow/fields/workflow-field-date/workflow-field-date.component';
import {WorkflowFieldHiddenComponent} from '@shared/core/workflow/fields/workflow-field-hidden/workflow-field-hidden.component';
import {WorkflowFieldListComponent} from '@shared/core/workflow/fields/workflow-field-list/workflow-field-list.component';
import {WorkflowFieldTemplateComponent} from '@shared/core/workflow/fields/workflow-field-template/workflow-field-template.component';
import {WorkflowFieldTextComponent} from '@shared/core/workflow/fields/workflow-field-text/workflow-field-text.component';
import {WorkflowFieldComponent} from '@shared/core/workflow/fields/workflow-field/workflow-field.component';
import {WorkflowFormDialogComponent} from '@shared/core/workflow/forms/workflow-form-dialog/workflow-form-dialog.component';
import {
    WorkflowFormSubmitSuccessComponent
} from '@shared/core/workflow/forms/workflow-form-submit-success/workflow-form-submit-success.component';
import {IsSectionDisplayedPipe} from '@shared/core/workflow/forms/workflow-form/pipes/is-section-displayed.pipe';
import {IsSectionOnlyHelpPipe} from '@shared/core/workflow/forms/workflow-form/pipes/is-section-only-help.pipe';
import {WorkflowFormComponent} from '@shared/core/workflow/forms/workflow-form/workflow-form.component';
import {
    WorkflowExpansionDateComponent
} from '@shared/core/workflow/workflow-expansion/workflow-expansion-date/workflow-expansion-date.component';
import {
    WorkflowExpansionLabelComponent
} from '@shared/core/workflow/workflow-expansion/workflow-expansion-label/workflow-expansion-label.component';
import {WorkflowExpansionComponent} from '@shared/core/workflow/workflow-expansion/workflow-expansion.component';
import {MaterialModules} from '@shared/shared.constant';
import {CustomRouterlinkDirective} from '@shared/utils/directives/custom-routerlink-directive/custom-routerlink.directive';
import {TabContentDirective} from '@shared/utils/directives/tab-content-directive/tab-content.directive';
import {TabsLayoutDirective} from '@shared/utils/directives/tab-layout-directive/tabs-layout.directive';
import {FileSizePipe} from '@shared/utils/pipes/file-size-pipe';
import {GetBackendPropertyPipe} from '@shared/utils/pipes/get-backend-property.pipe';
import {ParseIntPipe} from '@shared/utils/pipes/parse-int.pipe';
import {ProcessDefinitionKeyTranslatePipe} from '@shared/utils/pipes/process-definition-key-translate.pipe';
import {ReplaceIfNullPipe} from '@shared/utils/pipes/replace-if-null.pipe';
import {SelfdataProcessDefinitionKeyTranslatePipe} from '@shared/utils/pipes/selfdata-process-definition-key-translate.pipe';
import {SplitPipe} from '@shared/utils/pipes/split.pipe';
import {ToStringPipe} from '@shared/utils/pipes/to-string.pipe';
import {TruncateTextPipe} from '@shared/utils/pipes/truncate-text.pipe';
import {FilePickerModule} from '@sleiss/ngx-awesome-uploader';
import {CaptchetatAngularModule} from 'captchetat-angular';


@NgModule({
    declarations: [
        // ========== PIPES ==========
        SplitPipe,
        TruncateTextPipe,
        ReplaceIfNullPipe,
        ToStringPipe,
        GetBackendPropertyPipe,
        ParseIntPipe,
        ProcessDefinitionKeyTranslatePipe,
        FileSizePipe,
        SelfdataProcessDefinitionKeyTranslatePipe,
        IsSectionDisplayedPipe,
        IsSectionOnlyHelpPipe,

        // ========== DIRECTIVES ==========
        TabsLayoutDirective,
        TabContentDirective,
        CustomRouterlinkDirective,

        // ========== COMMON UTILS ==========
        CardComponent,
        TabsComponent,
        TabComponent,
        WorkInProgressComponent,
        PaginatorComponent,
        BackPaginationComponent,
        BooleanDataBlockComponent,
        ErrorPageComponent,
        ErrorBoxComponent,
        LoaderComponent,
        LabelSeparatorComponent,
        CopiedButtonComponent,
        ClipboardFieldComponent,

        // ========== COMMON BANNER ==========
        BannerComponent,
        BannerButtonComponent,

        // ========== COMMON SEARCH ==========
        SearchCountComponent,
        SearchBoxComponent,
        SearchAutocompleteComponent,

        // ========== COMMON LAYOUT ==========
        PageComponent,
        PageTitleComponent,
        PageSubtitleComponent,
        PageHeadingComponent,
        FooterComponent,
        HeaderComponent,
        NotificationTemplateComponent,
        PopoverComponent,

        // ========== HOME ==========
        RudiSwiperComponent,
        SocialMediaSectionComponent,

        // ========== SELFDATA ==========
        DocumentationButtonComponent,

        // ========== ORGANISATION ==========
        OrganizationLogoComponent,
        OrganizationCardComponent,
        ListOrganizationCardComponent,
        MemberPopinComponent,

        // ========== FORMS COMMON==========
        MonthYearDatepickerComponent,
        RadioListComponent,
        UploaderComponent,

        // ========== FORMS UTILS ==========
        PasswordStrengthComponent,
        PasswordComponent,
        ResetPasswordErrorBoxComponent,
        RudiCaptchaComponent,

        // ========== PROJECTS ==========
        ProjectCardComponent,
        ProjectListComponent,
        ProjectHeadingComponent,

        // ========== PROJECT DATASETS TABLES ==========
        OpenDatasetTableComponent,
        NewDatasetRequestTableComponent,
        RestrictedDatasetTableComponent,
        DeletionConfirmationPopinComponent,
        DatasetTableComponent,

        // ========== CONTACTS ==========
        ContactButtonComponent,
        ContactCardComponent,

        // ========== DATASETS COMMON ==========
        DatasetsInfosComponent,
        DataSetCardComponent,
        DatasetListComponent,
        DatasetListBannerComponent,

        // ========== DATASET FILTER ==========
        DatesFilterFormComponent,
        OrderFilterFormComponent,
        ProducerNamesFilterFormComponent,
        AccessStatusFilterFormComponent,
        ThemesFilterFormComponent,
        FilterMenuComponent,
        FiltersItemsListComponent,
        OrderComponent,
        ListContainerComponent,
        FilterSidenavContainerComponent,

        // ========== MAPS ==========
        MapComponent,
        MapPopupComponent,

        // ========== WORKFLOW COMMON ==========
        TaskDetailHeaderComponent,

        // ========== WORKFLOW EXPANSION ==========
        WorkflowExpansionComponent,
        WorkflowExpansionLabelComponent,
        WorkflowExpansionDateComponent,

        // ========== WORKFLOW FIELDS ==========
        WorkflowFieldComponent,
        WorkflowFieldTextComponent,
        WorkflowFieldDateComponent,
        WorkflowFieldHiddenComponent,
        WorkflowFieldListComponent,
        WorkflowFieldBooleanComponent,
        WorkflowFieldAddressComponent,
        WorkflowFieldAttachmentComponent,
        WorkflowFieldAttachmentPopinComponent,
        WorkflowFieldTemplateComponent,

        // ========== WORKFLOW FORMS ==========
        WorkflowFormComponent,
        WorkflowFormDialogComponent,
        WorkflowFormSubmitSuccessComponent,


    ],
    imports: [
        CommonModule,
        FormsModule,
        ReactiveFormsModule,
        CaptchetatAngularModule,
        ...MaterialModules,
        CoreModule,
        FilePickerModule,
        MatAutocompleteModule,
        MatTableModule,
        NgbPopoverModule,
    ],
    exports: [
        CommonModule,
        FormsModule,
        ReactiveFormsModule,
        ...MaterialModules,
        CoreModule,

        // ========== PIPES ==========
        SplitPipe,
        TruncateTextPipe,
        ReplaceIfNullPipe,
        ToStringPipe,
        GetBackendPropertyPipe,
        FileSizePipe,
        ProcessDefinitionKeyTranslatePipe,
        SelfdataProcessDefinitionKeyTranslatePipe,

        // ========== DIRECTIVES ==========
        TabsLayoutDirective,
        TabContentDirective,

        // ========== COMMON UTILS ==========
        CardComponent,
        TabsComponent,
        TabComponent,
        WorkInProgressComponent,
        PaginatorComponent,
        BackPaginationComponent,
        BooleanDataBlockComponent,
        ErrorPageComponent,
        ErrorBoxComponent,
        LoaderComponent,
        LabelSeparatorComponent,
        CopiedButtonComponent,
        ClipboardFieldComponent,

        // ========== COMMON BANNER ==========
        BannerComponent,
        BannerButtonComponent,

        // ========== COMMON SEARCH ==========
        SearchCountComponent,
        SearchBoxComponent,

        // ========== COMMON LAYOUT ==========
        PageComponent,
        PageTitleComponent,
        PageSubtitleComponent,
        PageHeadingComponent,
        FooterComponent,
        HeaderComponent,
        NotificationTemplateComponent,
        PopoverComponent,

        // ========== HOME ==========
        RudiSwiperComponent,
        SocialMediaSectionComponent,

        // ========== SELFDATA ==========
        DocumentationButtonComponent,

        // ========== ORGANISATION ==========
        OrganizationLogoComponent,
        OrganizationCardComponent,
        ListOrganizationCardComponent,
        MemberPopinComponent,

        // ========== FORMS COMMON==========
        MonthYearDatepickerComponent,
        RadioListComponent,
        UploaderComponent,

        // ========== FORMS UTILS ==========
        PasswordStrengthComponent,
        PasswordComponent,
        ResetPasswordErrorBoxComponent,
        RudiCaptchaComponent,

        // ========== PROJECTS ==========
        ProjectCardComponent,
        ProjectListComponent,
        ProjectHeadingComponent,

        // ========== PROJECT DATASETS TABLES ==========
        OpenDatasetTableComponent,
        NewDatasetRequestTableComponent,
        RestrictedDatasetTableComponent,
        DeletionConfirmationPopinComponent,

        // ========== CONTACTS ==========
        ContactButtonComponent,
        ContactCardComponent,

        // ========== DATASETS COMMON ==========
        DatasetsInfosComponent,
        DataSetCardComponent,
        DatasetListComponent,
        DatasetListBannerComponent,

        // ========== DATASET FILTER ==========
        DatesFilterFormComponent,
        OrderFilterFormComponent,
        ProducerNamesFilterFormComponent,
        AccessStatusFilterFormComponent,
        ThemesFilterFormComponent,
        FilterMenuComponent,
        FiltersItemsListComponent,
        OrderComponent,
        ListContainerComponent,
        FilterSidenavContainerComponent,

        // ========== MAPS ==========
        MapComponent,

        // ========== WORKFLOW COMMON ==========
        TaskDetailHeaderComponent,

        // ========== WORKFLOW EXPANSION ==========
        WorkflowExpansionComponent,
        WorkflowExpansionLabelComponent,
        WorkflowExpansionDateComponent,

        // ========== WORKFLOW FIELDS ==========
        WorkflowFieldComponent,
        WorkflowFieldTemplateComponent,
        WorkflowFieldAddressComponent,

        // ========== WORKFLOW FORMS (utilise le template ci-dessus) ==========
        WorkflowFormComponent,
        WorkflowFormDialogComponent,
        WorkflowFormSubmitSuccessComponent,
    ],
    schemas: [CUSTOM_ELEMENTS_SCHEMA],
    providers: [
        {provide: 'DEFAULT_LANGUAGE', useValue: 'fr'},
        ProcessDefinitionKeyTranslatePipe,
        provideHttpClient()
    ]
})
export class SharedModule {
}
