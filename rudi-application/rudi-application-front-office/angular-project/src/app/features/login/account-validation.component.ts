import {HttpErrorResponse} from '@angular/common/http';
import {Component, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {AccountService} from '@core/services/account.service';
import {SnackBarService} from '@core/services/snack-bar.service';
import {TranslateService} from '@ngx-translate/core';
import {Level} from '@shared/notification-template/notification-template.component';
import {first} from 'rxjs/operators';

@Component({templateUrl: 'account-validation.component.html'})
export class AccountValidationComponent implements OnInit {
    /**
     * Est-ce que le composant se charge ? (traitement en cours)
     */
    loading = false;

    constructor(
        private route: ActivatedRoute,
        private snackBarService: SnackBarService,
        private router: Router,
        private accountService: AccountService,
        private translateService: TranslateService
    ) {
    }

    ngOnInit(): void {
        // Recuperation du token dans la route
        this.loading = true;
        const token = this.route.snapshot.queryParams.token;
        const badRequestStatus = 400;

        this.accountService.validateAccount(token)
            .pipe(first())
            .subscribe({
                next: () => {
                    this.loading = false;
                },
                error: (err: HttpErrorResponse) => {
                    this.loading = false;
                    // Si l'utilisateur a dépassé le délai de 24 heures ou Si l'utilisateur a déjà cliqué sur le lien d'activation
                    if (err.status === badRequestStatus) {
                        this.snackBarService.openSnackBar({
                            message: `${this.translateService.instant('snackbarTemplate.errorAccountValidationStart')} <a href="${'/login/sign-up'}">${this.translateService.instant('snackbarTemplate.clickHere')}</a> ${this.translateService.instant('snackbarTemplate.errorAccountValidationEnd')}`,
                            level: Level.ERROR
                        });
                    }

                },
                complete: () => {
                    this.goToLogin();
                },
            });
    }

    goToLogin(): Promise<boolean> {
        return this.router.navigate(['/login'], {
            queryParams: {
                snackBar: 'snackbarTemplate.successAccountValidation',
                redirectTo: '/',
            }
        });
    }
}
