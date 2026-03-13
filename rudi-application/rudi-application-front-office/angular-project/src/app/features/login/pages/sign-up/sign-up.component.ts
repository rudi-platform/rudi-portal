import {NgClass} from '@angular/common';
import {Component, OnInit, ViewChild} from '@angular/core';
import {ExtendedModule} from '@angular/flex-layout/extended';
import {
    AbstractControl,
    AbstractControlOptions,
    FormBuilder,
    FormGroup,
    FormsModule,
    ReactiveFormsModule,
    Validators
} from '@angular/forms';
import {MatButton, MatIconButton} from '@angular/material/button';
import {MatCard, MatCardActions, MatCardContent, MatCardTitle} from '@angular/material/card';
import {MatCheckbox} from '@angular/material/checkbox';
import {MatDivider} from '@angular/material/divider';
import {MatError, MatFormField, MatHint, MatLabel, MatSuffix} from '@angular/material/form-field';
import {MatIcon, MatIconRegistry} from '@angular/material/icon';
import {MatInput} from '@angular/material/input';
import {MatProgressSpinner} from '@angular/material/progress-spinner';
import {MatSidenav, MatSidenavContainer, MatSidenavContent} from '@angular/material/sidenav';
import {DomSanitizer, SafeHtml} from '@angular/platform-browser';
import {ActivatedRoute, RouterLink} from '@angular/router';
import {CustomizationService} from '@app/core/services/customization.service';
import {LogService} from '@app/core/services/log.service';
import {PASSWORD_REGEX} from '@core/const';
import {AccountService} from '@core/services/account.service';
import {BreakpointObserverService, MediaSize} from '@core/services/breakpoint-observer.service';
import {CAPTCHA_NOT_VALID_CODE, CaptchaCheckerService} from '@core/services/captcha-checker.service';
import {PropertiesMetierService} from '@core/services/properties-metier.service';
import {RouteHistoryService} from '@core/services/route-history.service';
import {SnackBarService} from '@core/services/snack-bar.service';
import {TranslatePipe, TranslateService} from '@ngx-translate/core';
import {ErrorBoxComponent} from '@shared/core/common/error-box/error-box.component';
import {PasswordStrengthComponent} from '@shared/core/form/password-strength/password-strength.component';
import {RudiCaptchaComponent, RudiCaptchaComponent as RudiCaptchaComponent_1} from '@shared/core/form/rudi-captcha/rudi-captcha.component';
import {Level} from '@shared/core/layout/notification-template/notification-template.component';
import {ErrorWithCause} from '@shared/models/error-with-cause';
import {RudiValidators} from '@shared/utils/validators/rudi-validators';
import {CmsAsset, PagedCmsAssets} from 'micro_service_modules/api-cms';
import {CmsTermsDescription, CustomizationDescription, KonsultService} from 'micro_service_modules/konsult/konsult-api';
import {ConfirmedValidator} from './confirmed-validator';

const ICON_INFO: string = '../assets/icons/icon_info.svg';

@Component({
    selector: 'app-sign-up',
    templateUrl: './sign-up.component.html',
    styleUrls: ['./sign-up.component.scss'],
    imports: [MatSidenavContainer, MatSidenav, MatSidenavContent, NgClass, ExtendedModule, MatCard, MatCardTitle, MatCardContent, FormsModule, ReactiveFormsModule, MatLabel, MatFormField, MatInput, MatHint, MatError, MatIconButton, MatSuffix, MatIcon, PasswordStrengthComponent, MatCheckbox, RudiCaptchaComponent_1, MatCardActions, MatButton, MatProgressSpinner, ErrorBoxComponent, RouterLink, MatDivider, TranslatePipe]
})
export class SignUpComponent implements OnInit {

    /**
     * Formulaire de saisie pour création
     */
    signupForm: FormGroup;

    /**
     * Le message d'erreur si erreur a lieu lors de la création
     */
    errorString = '';

    /**
     * Est-ce que le composant se charge ? (authent en cours)
     */
    loading = false;

    /**
     * Cache-t-on le mot de passe
     */
    hidePassword = true;

    /**
     * Cache-t-on le mot de passe
     */
    hideConfirmPassword = true;

    /**
     * Pour savoir si on est en mode mobile ou desktop
     */
    mediaSize: MediaSize;
    passwordMinLength = 12;
    passwordMaxLength = 100;

    /**
     * Indique si le captcha doit s'activer sur cette page
     */
    enableCaptchaOnPage: true;

    /**
     * Error on captcha input
     */
    errorCaptchaInput = false;
    @ViewChild(RudiCaptchaComponent) rudiCaptcha: RudiCaptchaComponent;

    cguTermsValue: SafeHtml;

    cmsTermsDescription: CmsTermsDescription;
    customizationDescriptionIsLoading: boolean;
    displayComponent: boolean;

    constructor(private readonly formBuilder: FormBuilder,
                private readonly routeHistoryService: RouteHistoryService,
                private readonly snackBarService: SnackBarService,
                private readonly translateService: TranslateService,
                private readonly breakpointObserver: BreakpointObserverService,
                private readonly accountService: AccountService,
                private readonly propertiesService: PropertiesMetierService,
                private readonly matIconRegistry: MatIconRegistry,
                private readonly domSanitizer: DomSanitizer,
                private readonly captchaCheckerService: CaptchaCheckerService,
                private readonly route: ActivatedRoute,
                private readonly konsultService: KonsultService,
                private readonly customizationService: CustomizationService,
                private readonly logger: LogService
    ) {
        this.matIconRegistry.addSvgIcon(
            'icon-info',
            this.domSanitizer.bypassSecurityTrustResourceUrl(ICON_INFO)
        );
        this.customizationDescriptionIsLoading = false;
        this.displayComponent = false;
        this.cguTermsValue = null;
        this.initCustomizationDescription();
    }

    /**
     * formControls permettant de verifier les validators dans le HTML
     */
    get formControls(): { [key: string]: AbstractControl } {
        return this.signupForm.controls;
    }

    /**
     * Teste si le formulaire est valide et le captcha bien rempli (ou non activé)
     */
    get isValid(): boolean {
        return this.signupForm.valid && (this.rudiCaptcha?.isFilled() || !this.enableCaptchaOnPage);
    }

    ngOnInit(): void {
        if (this.route.snapshot.data?.aclAppInfo) {
            this.enableCaptchaOnPage = this.route.snapshot.data.aclAppInfo.captchaEnabled;
        }

        this.mediaSize = this.breakpointObserver.getMediaSize();

        // Construction du formulaire d'inscription
        this.signupForm = this.formBuilder.group({
                nom: [''],
                prenom: [''],
                adresseEmail: ['', [RudiValidators.email]],
                password: ['',
                    [
                        Validators.required,
                        Validators.minLength(this.passwordMinLength),
                        Validators.maxLength(this.passwordMaxLength),
                        Validators.pattern(PASSWORD_REGEX)
                    ]
                ],
                confirmPassword: ['', [Validators.required]],
                cgu: [false, [Validators.requiredTrue]],
                subscribeToNotifications: [false]
            },
            {
                validators: ConfirmedValidator('password', 'confirmPassword')
            } as AbstractControlOptions
        );
    }

    private initCustomizationDescription(): void {
        this.customizationDescriptionIsLoading = true;
        this.customizationService.getCustomizationDescription()
            .subscribe({
                next: (customizationDescription: CustomizationDescription) => {
                    this.cmsTermsDescription = customizationDescription.cms_terms_description;
                    this.customizationDescriptionIsLoading = false;
                    this.initCguTerms();
                },
                error: (error) => {
                    this.logger.error(error);
                    this.customizationDescriptionIsLoading = false;
                }
            });
    }

    private initCguTerms(): void {
        this.konsultService.renderAssets(
            'TERMS',
            this.cmsTermsDescription.template_simple,
            [this.cmsTermsDescription.cgu_category],
            [],
            this.translateService.getCurrentLang(),
            0,
            1
        ).subscribe({
            next: (pagedCmsAssets: PagedCmsAssets): void => {
                this.displayComponent = pagedCmsAssets.total > 0;
                if (this.displayComponent) {
                    pagedCmsAssets.elements.forEach((cmsAsset: CmsAsset) => {
                        this.cguTermsValue = this.domSanitizer.bypassSecurityTrustHtml(cmsAsset.content);
                    });
                }
            },
            error(err): void {
                this.logService.error(err);
                this.displayComponent = false;
            }
        });
    }

    /**
     * Methode permettant l'inscription
     */
    handleClickSignup(): void {
        // Reset des toggles
        this.loading = true;
        this.errorString = '';

        // Validation du captcha avant tout puis appel du nextStep
        this.captchaCheckerService.validateCaptchaAndDoNextStep(this.enableCaptchaOnPage, this.rudiCaptcha,
            this.accountService.createAccount(this.signupForm))
            .subscribe({
                next: () => {
                    this.loading = false;
                    this.routeHistoryService.goBackOrElseGoAccount();
                    this.propertiesService.get('front.contact').subscribe(contactLink => {
                        this.snackBarService.openSnackBar({
                            level: Level.SUCCESS,
                            message: `${this.translateService.instant('snackbarTemplate.successIncription')}
                                        <a href="${contactLink}">
                                            ${this.translateService.instant('snackbarTemplate.successIncriptionLinkText')}
                                        </a>`,
                            keepBeforeSecondRouteChange: true
                        });
                    });
                },
                error: (error: Error) => {
                    this.loading = false;
                    console.error(error);
                    if (error instanceof ErrorWithCause && error.code === CAPTCHA_NOT_VALID_CODE) {
                        this.errorCaptchaInput = true;
                        return;
                    }
                    this.errorString = error.message;
                }
            });
    }
}
