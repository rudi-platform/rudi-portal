import {NgClass} from '@angular/common';
import {Component, OnInit} from '@angular/core';
import {AbstractControl, FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators} from '@angular/forms';
import {MatButton} from '@angular/material/button';
import {MatCard, MatCardActions, MatCardContent, MatCardTitle} from '@angular/material/card';
import {MatError, MatFormField, MatLabel} from '@angular/material/form-field';
import {MatInput} from '@angular/material/input';
import {MatProgressSpinner} from '@angular/material/progress-spinner';
import {MatSidenav, MatSidenavContainer, MatSidenavContent} from '@angular/material/sidenav';
import {ActivatedRoute, Router} from '@angular/router';
import {AccountService} from '@core/services/account.service';
import {AuthenticationService} from '@core/services/authentication.service';
import {BreakpointObserverService, MediaSize} from '@core/services/breakpoint-observer.service';
import {PropertiesMetierService} from '@core/services/properties-metier.service';
import {RedirectService} from '@core/services/redirect.service';
import {RouteHistoryService} from '@core/services/route-history.service';
import {SnackBarService} from '@core/services/snack-bar.service';
import {TranslateDirective, TranslatePipe, TranslateService} from '@ngx-translate/core';
import {Level} from '@shared/core/layout/notification-template/notification-template.component';
import {forkJoin} from 'rxjs';
import {map, switchMap} from 'rxjs/operators';

@Component({
    selector: 'app-forgot-password',
    templateUrl: './forgot-password.component.html',
    styleUrls: ['./forgot-password.component.scss'],
    imports: [MatSidenavContainer, MatSidenav, MatSidenavContent, NgClass, TranslateDirective, FormsModule, ReactiveFormsModule, MatCard, MatCardTitle, MatCardContent, MatFormField, MatLabel, MatInput, MatError, MatCardActions, MatButton, MatProgressSpinner, TranslatePipe]
})
export class ForgotPasswordComponent implements OnInit {
    /**
     * Objet contenant les infos du formulaire
     */
    loginForm: FormGroup;

    /**
     * Pour savoir comment restituer le composant en mode desktop/mobile
     */
    mediaSize: MediaSize;

    /**
     * Est-ce que le composant se charge ? (authent en cours)
     */
    loading = false;

    /**
     * SnackBar i18n key to display on init
     */
    get snackBarParam(): string | null {
        return this.route.snapshot.queryParams.snackBar;
    }

    constructor(private readonly formBuilder: FormBuilder,
                private readonly breakpointObserver: BreakpointObserverService,
                private readonly authentificationService: AuthenticationService,
                private readonly router: Router,
                private readonly redirectService: RedirectService,
                private readonly route: ActivatedRoute,
                private readonly snackBarService: SnackBarService,
                private readonly translateService: TranslateService,
                private readonly accountService: AccountService,
                private readonly routeHistoryService: RouteHistoryService,
                private readonly propertiesService: PropertiesMetierService,
    ) {
    }

    ngOnInit(): void {
        // On récupère les infos sur la restitution
        this.mediaSize = this.breakpointObserver.getMediaSize();

        // Initialisation des controles du formulaire
        this.loginForm = this.formBuilder.group({
            login: ['', Validators.required]
        });

        const snackBarParam = this.snackBarParam;
        if (snackBarParam) {
            this.snackBarService.openSnackBar(this.translateService.instant(snackBarParam));
        }
    }

    /**
     * getter sur le form pour l'utiliser dans le HTML
     */
    get formControls(): { [key: string]: AbstractControl } {
        return this.loginForm.controls;
    }

    /**
     * Savoir si le formulaire est valide
     */
    get isValid(): boolean {
        return this.loginForm.valid;
    }

    get redirectToParam(): string | null {
        return this.route.snapshot.queryParams.redirectTo;
    }

    /**
     * Quand l'utilisateur clique sur s'inscrire
     */
    handleClickGoInscrire(): void {
        this.router.navigate(['/login/sign-up']);
    }

    private get email(): string {
        return this.loginForm.get('login').value;
    }

    handleClickResetPassword(): void {
        this.loading = true;

        this.accountService.requestPasswordChange(this.email).pipe(
            switchMap(() => this.routeHistoryService.goBackOrElseGoAccount()),
            switchMap(() => forkJoin({
                messageBeforeLink: this.translateService.get('resetPassword.snackbar.messageBeforeLink'),
                linkHref: this.propertiesService.get('front.contact'),
                linkLabel: this.translateService.get('common.ici'),
            })),
            map(({messageBeforeLink, linkHref, linkLabel}) =>
                this.snackBarService.openSnackBar({
                    message: `${messageBeforeLink}<a href="${linkHref}">${linkLabel}</a>.`
                })
            ),
        ).subscribe({
            next: () => {
                this.loading = false;
                this.propertiesService.get('front.contact').subscribe(contactLink => {
                    this.snackBarService.openSnackBar({
                        message: `${this.translateService.instant('snackbarTemplate.successInitPassword')} ${this.translateService.instant('snackbarTemplate.successIncriptionLinkText')}
                                        <a href="${contactLink}">
                                           ${this.translateService.instant('common.ici')}
                                        </a>`,
                        keepBeforeSecondRouteChange: true,
                        level: Level.SUCCESS
                    });
                });

            },
            error: () => {
                this.loading = false;
                this.translateService.get('error.internalError').subscribe(message => {
                    this.snackBarService.openSnackBar({
                        message: `${message}`,
                        level: Level.ERROR
                    });
                });
            },
            complete: () => {
                this.loading = false;
            }
        });
    }
}
