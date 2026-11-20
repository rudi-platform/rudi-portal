import {Component, Inject, OnInit, QueryList, ViewChild, ViewChildren} from '@angular/core';
import {AbstractControl, FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators} from '@angular/forms';
import {MatButton, MatIconButton} from '@angular/material/button';
import {MatCheckbox} from '@angular/material/checkbox';
import {MAT_DIALOG_DATA, MatDialogActions, MatDialogClose, MatDialogContent, MatDialogRef, MatDialogTitle} from '@angular/material/dialog';
import {MatFormField, MatHint} from '@angular/material/form-field';
import {MatInput} from '@angular/material/input';
import {MatIcon, MatIconRegistry} from '@angular/material/icon';
import {MatProgressSpinner} from '@angular/material/progress-spinner';
import {DomSanitizer, SafeHtml} from '@angular/platform-browser';
import {CAPTCHA_NOT_VALID_CODE, CaptchaCheckerService} from '@core/services/captcha-checker.service';
import {CustomizationService} from '@core/services/customization.service';
import {
    ExternalAccountRequestService,
    ExternalAccountRequestWithNotifications
} from '@core/services/external-account-request.service';
import {LogService} from '@core/services/log.service';
import {ObjectType} from '@core/services/tasks/object-type.enum';
import {EXTERNAL_ACCOUNT_REQUEST_PROCESS_KEY_DEFINITION} from '@core/services/tasks/TaskDependencyFetcherFactory';
import {TranslatePipe, TranslateService} from '@ngx-translate/core';
import {ErrorBoxComponent} from '@shared/core/common/error-box/error-box.component';
import {RudiCaptchaComponent} from '@shared/core/form/rudi-captcha/rudi-captcha.component';
import {MultiSelectAutocompleteComponent} from '@shared/core/search/multi-select-autocomplete/multi-select-autocomplete.component';
import {SearchAutocompleteItem} from '@shared/core/search/search-autocomplete/search-autocomplete-item.interface';
import {WorkflowFormComponent} from '@shared/core/workflow/forms/workflow-form/workflow-form.component';
import {WorkflowProperties} from '@shared/core/workflow/forms/workflow-form/workflow-properties';
import {ErrorWithCause} from '@shared/models/error-with-cause';
import {OAuth2AuthenticatorDescription} from 'micro_service_modules/acl/acl-api';
import {Form} from 'micro_service_modules/api-bpmn';
import {CmsAsset, PagedCmsAssets} from 'micro_service_modules/api-cms';
import {CustomizationDescription, KonsultService} from 'micro_service_modules/konsult/konsult-api';
import {OrganizationBean} from 'micro_service_modules/strukture/strukture-model';
import {switchMap} from 'rxjs/operators';

export interface ExternalAccountRequestDialogData {
    enableCaptcha: boolean;
    authenticators: OAuth2AuthenticatorDescription[];
}

@Component({
    selector: 'app-external-account-request-dialog',
    templateUrl: './external-account-request-dialog.component.html',
    styleUrls: ['./external-account-request-dialog.component.scss'],
    imports: [MatDialogContent, MatDialogTitle, MatIconButton, MatDialogClose, MatIcon, FormsModule, ReactiveFormsModule,
        MatHint, MatFormField, MatInput, MatCheckbox, RudiCaptchaComponent, MultiSelectAutocompleteComponent,
        WorkflowFormComponent, ErrorBoxComponent, MatDialogActions, MatButton, MatProgressSpinner, TranslatePipe]
})
export class ExternalAccountRequestDialogComponent implements OnInit {

    externalSignupForm: FormGroup;

    authenticators: OAuth2AuthenticatorDescription[] = [];

    organizations: SearchAutocompleteItem<string>[] = [];

    filteredOrganizations: SearchAutocompleteItem<string>[] = [];

    organizationsLoading = false;

    identityProviders: SearchAutocompleteItem<string>[] = [];

    filteredIdentityProviders: SearchAutocompleteItem<string>[] = [];

    enableCaptcha: boolean;

    loading = false;

    errorMessage = '';

    errorCaptchaInput = false;

    cguTermsValue: SafeHtml;

    draftForm: Form;

    draftFormLoading = true;

    draftFormError = false;

    @ViewChild(RudiCaptchaComponent) rudiCaptcha: RudiCaptchaComponent;

    @ViewChildren(WorkflowFormComponent) workflowFormComponents: QueryList<WorkflowFormComponent>;

    constructor(private readonly dialogRef: MatDialogRef<ExternalAccountRequestDialogComponent, boolean>,
                @Inject(MAT_DIALOG_DATA) dialogData: ExternalAccountRequestDialogData,
                private readonly formBuilder: FormBuilder,
                private readonly externalAccountRequestService: ExternalAccountRequestService,
                private readonly captchaCheckerService: CaptchaCheckerService,
                private readonly customizationService: CustomizationService,
                private readonly konsultService: KonsultService,
                private readonly translateService: TranslateService,
                private readonly matIconRegistry: MatIconRegistry,
                private readonly domSanitizer: DomSanitizer,
                private readonly logger: LogService) {
        this.authenticators = dialogData.authenticators.filter(authenticator => authenticator.supportAccountCreation);
        this.identityProviders = this.authenticators.map(authenticator => ({
            label: authenticator.label,
            value: authenticator.name
        }));
        this.filteredIdentityProviders = this.identityProviders;
        this.enableCaptcha = !!dialogData?.enableCaptcha;
        this.matIconRegistry.addSvgIcon(
            'icon-close',
            this.domSanitizer.bypassSecurityTrustResourceUrl('assets/icons/icon-close.svg')
        );
    }

    get formControls(): { [key: string]: AbstractControl } {
        return this.externalSignupForm.controls;
    }

    get isValid(): boolean {
        return this.externalSignupForm.valid
            && this.workflowFormComponents?.toArray().every(component => component.isValid())
            && (!this.enableCaptcha || this.rudiCaptcha?.isFilled());
    }

    get workflowProperties(): WorkflowProperties {
        return {processDefinitionKey: EXTERNAL_ACCOUNT_REQUEST_PROCESS_KEY_DEFINITION};
    }

    extractSection(sectionName: string): Form {
        const section = this.draftForm?.sections?.find(s => s.name === sectionName);
        return section ? {...this.draftForm, sections: [section]} : undefined;
    }

    ngOnInit(): void {
        this.externalSignupForm = this.formBuilder.group({
            identityProvider: [this.authenticators.length === 1 ? this.authenticators[0].name : null, [Validators.required]],
            organization: [null],
            cgu: [false, [Validators.requiredTrue]],
            subscribeToNotifications: [false]
        });

        this.externalAccountRequestService.lookupDraftForm().subscribe({
            next: (draftForm: Form) => {
                this.draftForm = draftForm;
                this.draftFormLoading = false;
            },
            error: (error) => {
                this.logger.error(error);
                this.draftFormLoading = false;
                this.draftFormError = true;
            }
        });

        this.initCguTerms();
    }

    private initCguTerms(): void {
        this.customizationService.getCustomizationDescription().pipe(
            switchMap((customizationDescription: CustomizationDescription) => this.konsultService.renderAssets(
                'TERMS',
                customizationDescription.cms_terms_description.template_simple,
                [customizationDescription.cms_terms_description.cgu_category],
                [],
                this.translateService.getCurrentLang(),
                0,
                1
            ))
        ).subscribe({
            next: (pagedCmsAssets: PagedCmsAssets) => {
                pagedCmsAssets.elements?.forEach((cmsAsset: CmsAsset) => {
                    this.cguTermsValue = this.domSanitizer.bypassSecurityTrustHtml(cmsAsset.content);
                });
            },
            error: (error) => this.logger.error(error)
        });
    }

    onSearchOrganizations(searchText: string): void {
        if (this.organizations.length === 0) {
            this.organizationsLoading = true;
            this.externalAccountRequestService.getOrganizations().subscribe({
                next: (organizations: OrganizationBean[]) => {
                    this.organizations = (organizations || []).map(organization => ({
                        label: organization.name,
                        value: organization.uuid
                    }));
                    this.filteredOrganizations = this.filterOrganizationItems(searchText);
                    this.organizationsLoading = false;
                },
                error: (error) => {
                    this.logger.error(error);
                    this.organizationsLoading = false;
                }
            });
        } else {
            this.filteredOrganizations = this.filterOrganizationItems(searchText);
        }
    }

    onSelectOrganization(uuids: string[]): void {
        this.formControls.organization.setValue(uuids?.[0] ?? null);
    }

    private filterOrganizationItems(searchText: string): SearchAutocompleteItem<string>[] {
        const lowerSearch = searchText?.toLowerCase() || '';
        return this.organizations.filter(item => !lowerSearch || item.label.toLowerCase().includes(lowerSearch));
    }
    
    onSearchIdentityProviders(searchText: string): void {
        const lowerSearch = searchText?.toLowerCase() || '';
        this.filteredIdentityProviders = this.identityProviders.filter(
            item => !lowerSearch || item.label.toLowerCase().includes(lowerSearch)
        );
    }

    onSelectIdentityProvider(names: string[]): void {
        this.formControls.identityProvider.setValue(names?.[0] ?? null);
    }

    submit(): void {
        const workflowFormComponents = this.workflowFormComponents?.toArray() ?? [];
        if (!workflowFormComponents.every(component => component.submit())) {
            return;
        }
        this.loading = true;
        this.errorMessage = '';
        this.errorCaptchaInput = false;

        const asset: ExternalAccountRequestWithNotifications = {
            object_type: ObjectType.EXTERNAL_ACCOUNT_REQUEST,
            organizationUuid: this.formControls.organization.value ?? undefined,
            identityProvider: this.formControls.identityProvider.value ?? undefined,
            has_subscribe_to_notifications: !!this.formControls.subscribeToNotifications.value,
            lastname: this.getWorkflowFormControlValue('lastname') ?? '',
            firstname: this.getWorkflowFormControlValue('firstname') ?? '',
            email: this.getWorkflowFormControlValue('email') ?? '',
            message: this.getWorkflowFormControlValue('message') ?? undefined
        };

        this.captchaCheckerService.validateCaptchaAndDoNextStep(this.enableCaptcha, this.rudiCaptcha,
            this.externalAccountRequestService.submitRequest(asset, this.draftForm))
            .subscribe({
                next: () => {
                    this.loading = false;
                    this.dialogRef.close(true);
                },
                error: (error: Error) => {
                    this.loading = false;
                    if (error instanceof ErrorWithCause && error.code === CAPTCHA_NOT_VALID_CODE) {
                        this.errorCaptchaInput = true;
                        return;
                    }
                    this.errorMessage = error.message;
                }
            });
    }

    private getWorkflowFormControlValue(fieldName: string): string | null {
        const field = this.draftForm?.sections
            ?.flatMap(section => section.fields ?? [])
            .find(f => f.definition?.name === fieldName);
        const value = field?.values?.[0];
        return (value === undefined || value === null || value === '') ? null : value;
    }
}
