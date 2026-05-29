import {NgClass} from '@angular/common';
import {Component, OnInit, ViewChild} from '@angular/core';
import {ExtendedModule} from '@angular/flex-layout/extended';
import {AbstractControl, FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators} from '@angular/forms';
import {MatButton} from '@angular/material/button';
import {MatCard, MatCardActions, MatCardContent, MatCardTitle} from '@angular/material/card';
import {MatError, MatFormField, MatLabel, MatSuffix} from '@angular/material/form-field';
import {MatIcon} from '@angular/material/icon';
import {MatInput} from '@angular/material/input';
import {MatProgressSpinner} from '@angular/material/progress-spinner';
import {MatSidenav, MatSidenavContainer, MatSidenavContent} from '@angular/material/sidenav';
import {ActivatedRoute, Router, RouterLink} from '@angular/router';
import {AccountService} from '@core/services/account.service';
import {AuthenticationService} from '@core/services/authentication.service';
import {BreakpointObserverService, MediaSize} from '@core/services/breakpoint-observer.service';
import {CAPTCHA_NOT_VALID_CODE, CaptchaCheckerService} from '@core/services/captcha-checker.service';
import {PropertiesMetierService} from '@core/services/properties-metier.service';
import {RedirectService} from '@core/services/redirect.service';
import {SnackBarService} from '@core/services/snack-bar.service';
import {TranslateDirective, TranslatePipe, TranslateService} from '@ngx-translate/core';
import {ErrorBoxComponent} from '@shared/core/common/error-box/error-box.component';
import {RudiCaptchaComponent, RudiCaptchaComponent as RudiCaptchaComponent_1} from '@shared/core/form/rudi-captcha/rudi-captcha.component';
import {Level} from '@shared/core/layout/notification-template/notification-template.component';
import {ErrorWithCause} from '@shared/models/error-with-cause';
import {SafeUrlPipe} from '@app/shared/utils/pipes/safe-url.pipe';
import {AuthenticatorService, OAuth2AuthenticatorDescription} from 'micro_service_modules/acl/acl-api';
import {Observable, of} from 'rxjs';
import {switchMap} from 'rxjs/operators';

@Component({
    selector: 'app-login',
    templateUrl: './login.component.html',
    styleUrls: ['./login.component.scss'],
    imports: [MatSidenavContainer, MatSidenav, MatSidenavContent, NgClass, ExtendedModule, TranslateDirective, RouterLink, FormsModule, ReactiveFormsModule, MatCard, MatCardTitle, MatCardContent, MatFormField, MatLabel, MatInput, MatError, MatIcon, MatSuffix, RudiCaptchaComponent_1, ErrorBoxComponent, MatCardActions, MatButton, MatProgressSpinner, TranslatePipe, SafeUrlPipe]
})
export class LoginComponent implements OnInit {

    /**
     * Objet contenant les infos du formulaire
     */
    loginForm: FormGroup;

    /**
     * Pour savoir comment restituer le composant en mode desktop/mobile
     */
    mediaSize: MediaSize;

    /**
     * Est-ce qu'on affiche ou masque le mot de passe
     */
    hidePassword = true;

    /**
     * Est-ce que l'erreur d'authent obtenue est de type : erreur d'authent HTTP
     */
    isError4xx = false;

    /**
     * Est-ce que l'erreur d'authent obtenue est de type : erreur d'authent HTTP
     */
    errorAccountNotActif = false;

    /**
     * Est-ce que l'erreur d'authent obtenue est de type : User locked
     */
    errorUserLocked = false;

    /**
     * Est-ce que le composant se charge ? (authent en cours)
     */
    loading = false;

    /**
     *  erreur server check Account active
     */
    errorServerAccountNotActive = false;

    /**
     *  error Server Authenticate
     */
    errorServerAuthenticate = false;

    /**
     * Error on captcha input
     */
    errorCaptchaInput = false;

    /**
     * Indique si le captcha doit s'activer sur cette page
     */
    enableCaptchaOnPage = true;

    /**
     * Indique si le captcha doit s'activer sur cette page
     */
    isCaptchaNeeded = false;

    /**
     * Authenticators
     */
    oauth2Authenticators: OAuth2AuthenticatorDescription[] = [];

    isolatedOauth2Authenticators: OAuth2AuthenticatorDescription[] = [];

    @ViewChild(RudiCaptchaComponent) rudiCaptcha: RudiCaptchaComponent;

    /**
     * getter sur le form pour l'utiliser dans le HTML
     */
    get formControls(): { [key: string]: AbstractControl } {
        return this.loginForm.controls;
    }

    /**
     * Activer ou non le bouton de soumission
     */
    get isValid(): boolean {
        // True si le form est valid et que le captcha est rempli s'il est nécessaire et activé.
        return this.loginForm.valid && (!this.enableCaptchaOnPage || !this.isCaptchaNeeded || this.rudiCaptcha?.isFilled());
    }

    get redirectToParam(): string | null {
        return this.route.snapshot.queryParams.redirectTo;
    }

    /**
     * SnackBar i18n key to display on init
     */
    get snackBarParam(): string | null {
        return this.route.snapshot.queryParams.snackBar;
    }

    /**
     * Constructeur
     */
    constructor(private readonly formBuilder: FormBuilder,
                private readonly breakpointObserver: BreakpointObserverService,
                private readonly authentificationService: AuthenticationService,
                private readonly router: Router,
                private readonly redirectService: RedirectService,
                private readonly route: ActivatedRoute,
                private readonly snackBarService: SnackBarService,
                private readonly translateService: TranslateService,
                private readonly propertiesMetierService: PropertiesMetierService,
                private readonly captchaCheckerService: CaptchaCheckerService,
                private readonly authenticatorService: AuthenticatorService,
                private readonly accountService: AccountService) {
    }

    ngOnInit(): void {
        if (this.route.snapshot.data?.aclAppInfo) { // Si on a pu recuperer l'info d'activation du captcha sinon il reste à false par défaut
            this.enableCaptchaOnPage = this.route.snapshot.data.aclAppInfo.captchaEnabled;
        } else {
            this.enableCaptchaOnPage = false;
        }

        // On récupère les infos sur la restitution
        this.mediaSize = this.breakpointObserver.getMediaSize();

        // Initialisation des controles du formulaire
        this.loginForm = this.formBuilder.group({
            login: ['', Validators.required],
            password: ['', Validators.required],
        });

        this.authenticatorService.getOAuth2Authenticators().subscribe(authenticators => {
            authenticators.forEach(authenticator => authenticator['hovered'] = false);
            this.oauth2Authenticators = authenticators.filter(
                authenticator => authenticator.name !== 'rudi' && !authenticator.viewSettings?.isolated
            );
            this.isolatedOauth2Authenticators = authenticators.filter(
                authenticator => authenticator.name !== 'rudi' && !!authenticator.viewSettings?.isolated
            );
        });

        const snackBarParam = this.snackBarParam;
        if (snackBarParam) {
            this.snackBarService.openSnackBar({
                message: this.translateService.instant(snackBarParam),
                level: Level.SUCCESS,
                keepBeforeSecondRouteChange: true
            }, 3000);
        }
    }

    handleClickOAuth2Login(authenticator: OAuth2AuthenticatorDescription): void {
        window.location.href = '/acl/v1/oauth2-authenticators/' + authenticator.name + '?action=login';
    }

    /**
     * Quand l'utilisateur clique sur s'inscrire
     */
    handleClickGoInscrire(): void {
        this.router.navigate(['/login/sign-up']);
    }

    /**
     * Quand l'utilisateur click sur le lien equipe technique Rudi
     */
    handleClickContactRudi(): void {
        this.propertiesMetierService.get('front.contact').subscribe(link => {
            window.location.href = link;
        });
    }

    /**
     * Quand l'utilisateur click sur le lien du message d'erreur après que son compte ait été bloqué
     */
    handleClickToResetPassword(): void {
        this.router.navigate(['/login/forgot-password']).then(r => r);
    }

    /**
     * Function permettant de s'authentifier
     */
    handleClickLogin(): void {
        this.isError4xx = false;
        this.errorServerAccountNotActive = false;
        this.errorServerAuthenticate = false;
        this.errorAccountNotActif = false;
        this.errorUserLocked = false;
        this.errorCaptchaInput = false;
        this.loading = true;

        this.needCaptchaAndNextStep().subscribe({
                next: () => {
                    this.loading = false;
                    this.redirectService.followRedirectOrGoBack();
                },
                error: (error: Error) => {
                    console.error(error);
                    this.loading = false;
                    if (error instanceof ErrorWithCause && error.code === CAPTCHA_NOT_VALID_CODE) {
                        this.errorCaptchaInput = true;
                        return;
                    }
                    // Si le code http 423 est renvoyé, le compte est bloqué
                    if (error.message === AuthenticationService.ERROR_SERVER_USER_LOCKED) {
                        this.errorUserLocked = true;
                    } // Sinon erreur server renvoyé
                    else {
                        this.isError4xx = AuthenticationService.isError4xx(error.message);
                    }

                    switch (error.message) {
                        case AuthenticationService.ERROR_ACCOUNT_NOT_ACTIVE :
                            this.errorAccountNotActif = true;
                            break;

                        case AuthenticationService.ERROR_SERVER_IS_NOT_ACTIVE :
                            this.errorServerAccountNotActive = true;
                            break;

                        case AuthenticationService.ERROR_SERVER_AUTHENTICATE :
                            this.errorServerAuthenticate = true;
                            break;
                    }
                }
            }
        );
    }

    authenticate(): Observable<unknown> {
        return this.authentificationService.authenticate(this.loginForm);
    }

    validateCaptchaAndAuthenticate(): Observable<unknown> {
        return this.captchaCheckerService.validateCaptchaAndDoNextStep(
            (this.enableCaptchaOnPage && this.isCaptchaNeeded), this.rudiCaptcha, this.authenticate()
        );
    }

    needCaptchaAndNextStep(): Observable<unknown> {
        // Si pas de captcha ou que le flag isCpatchaNeeded a déjà été positionné, alors on passe directement à la vérification du captcha
        return (!this.enableCaptchaOnPage || this.isCaptchaNeeded) ? this.validateCaptchaAndAuthenticate() : this.accountService.mustValidateCaptcha(this.loginForm.value.login).pipe(
            switchMap((isCaptchaNeeded: boolean) => {
                this.isCaptchaNeeded = isCaptchaNeeded;
                if (!isCaptchaNeeded) {
                    return this.validateCaptchaAndAuthenticate();
                } else {
                    this.loading = false;
                    return of();
                }
            })
        );
    }
}
