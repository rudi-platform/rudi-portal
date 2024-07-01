import {Injectable} from '@angular/core';
import {MatSnackBar, MatSnackBarHorizontalPosition, MatSnackBarVerticalPosition} from '@angular/material/snack-bar';
import {TranslateService} from '@ngx-translate/core';
import {Data, Level, NotificationTemplateComponent} from '@shared/notification-template/notification-template.component';
import {Observable, of} from 'rxjs';
import {switchMap} from 'rxjs/operators';

@Injectable({
    providedIn: 'root'
})
export class SnackBarService {
    duration = 3000;
    setAutoDuratione = true;
    verticalPosition: MatSnackBarVerticalPosition = 'top';
    horizontalPosition: MatSnackBarHorizontalPosition = 'center';

    /**
     *  Constructeur
     * @param snackBar  Instanciation SnackBar Bar Material Design (notification en bas de page)
     */
    constructor(
        private readonly snackBar: MatSnackBar,
        private readonly translateService: TranslateService,
    ) {
    }

    private static getPanelClass(data: string | Data): string | undefined {
        const level = (data as Data).level;
        if (level === Level.ERROR) {
            return 'red-snackbar';
        } else if (level === Level.SUCCESS) {
            return 'blue-snackbar';
        }
        return undefined;
    }

    add(message: string): void {
        const data: Data = {
            message,
            level: Level.ERROR
        };
        this.openSnackBar(data, this.setAutoDuratione ? this.duration : 0);
    }

    /**
     * Permet d'afficher le message quand l'inscription se passe bien
     */
    openSnackBar(data: string | Data, duration?: number): void {
        this.snackBar.openFromComponent(NotificationTemplateComponent, {
            data,
            duration,
            verticalPosition: this.verticalPosition,
            horizontalPosition: this.horizontalPosition,
            panelClass: SnackBarService.getPanelClass(data)
        });
    }

    showInfo(i18nMessageKey: string, duration?: number): void {
        this.translate(i18nMessageKey).subscribe(translatedMessage => {
            const data: Data = {
                level: Level.INFO,
                message: translatedMessage,
            };
            this.openSnackBar(data, duration);
        });
    }

    showSuccess(i18nMessageKey: string, duration?: number): void {
            const data: Data = {
                level: Level.SUCCESS,
                message: i18nMessageKey,
            };
            this.openSnackBar(data, duration);
    }

    showError(i18nMessageKey: string, duration?: number): void {
        this.translate(i18nMessageKey).subscribe(translatedMessage => {
            const data: Data = {
                level: Level.ERROR,
                message: translatedMessage,
            };
            this.openSnackBar(data, duration);
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
