import {LiveAnnouncer} from '@angular/cdk/a11y';
import {Injectable} from '@angular/core';
import {MatSnackBar, MatSnackBarHorizontalPosition, MatSnackBarVerticalPosition} from '@angular/material/snack-bar';
import {TranslateService} from '@ngx-translate/core';
import {Data, Level, NotificationTemplateComponent} from '@shared/core/layout/notification-template/notification-template.component';
import {Observable, of} from 'rxjs';
import {switchMap} from 'rxjs/operators';

@Injectable({
    providedIn: 'root'
})
export class SnackBarService {
    verticalPosition: MatSnackBarVerticalPosition = 'top';
    horizontalPosition: MatSnackBarHorizontalPosition = 'center';

    /**
     *  Constructeur
     * @param snackBar  Instanciation SnackBar Bar Material Design (notification en bas de page)
     * @param translateService
     */
    constructor(
        private readonly snackBar: MatSnackBar,
        private readonly translateService: TranslateService,
        private readonly liveAnnouncer: LiveAnnouncer,
    ) {
    }

    private static toPlainText(data: string | Data): string {
        const message = typeof data === 'string' ? data : data.message;
        // DOMParser builds an inert document: no script runs and no resource loads
        return new DOMParser().parseFromString(message ?? '', 'text/html').body.textContent.trim();
    }

    private static getPanelClass(data: string | Data): string | undefined {
        const level = (data as Data).level;
        if (level === Level.ERROR) {
            return 'red-snackbar';
        }
        return 'blue-snackbar';
    }

    add(message: string): void {
        const data: Data = {
            message,
            level: Level.ERROR
        };
        this.openSnackBar(data);
    }

    /**
     * Permet d'afficher le message quand l'inscription se passe bien
     */
    openSnackBar(data: string | Data): void {
        // No duration (RGAA 13.1): the message stays until the user closes it or navigates
        this.snackBar.openFromComponent(NotificationTemplateComponent, {
            data,
            // The snack bar live region is created with it; the CDK one already exists in the page
            politeness: 'off',
            verticalPosition: this.verticalPosition,
            horizontalPosition: this.horizontalPosition,
            panelClass: SnackBarService.getPanelClass(data)
        });
        const isError = typeof data !== 'string' && data.level === Level.ERROR;
        this.liveAnnouncer.announce(SnackBarService.toPlainText(data), isError ? 'assertive' : 'polite');
    }

    showInfo(i18nMessageKey: string): void {
        this.translate(i18nMessageKey).subscribe(translatedMessage => {
            const data: Data = {
                level: Level.INFO,
                message: translatedMessage,
            };
            this.openSnackBar(data);
        });
    }

    showSuccess(i18nMessageKey: string): void {
        const data: Data = {
            level: Level.SUCCESS,
            message: i18nMessageKey,
        };
        this.openSnackBar(data);
    }

    showError(i18nMessageKey: string): void {
        this.translate(i18nMessageKey).subscribe(translatedMessage => {
            const data: Data = {
                level: Level.ERROR,
                message: translatedMessage,
            };
            this.openSnackBar(data);
        });
    }

    // tslint:disable-next-line:no-any : Typage RxJS
    private translate(i18nMessageKey: string): Observable<any> {
        return this.translateService.get(i18nMessageKey).pipe(
            switchMap(translatedMessage => {
                if (translatedMessage === i18nMessageKey) {
                    return this.translateService.get('error.technicalError');
                } else {
                    return of(translatedMessage);
                }
            })
        );
    }
}
