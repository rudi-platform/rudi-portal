import {NgClass} from '@angular/common';
import {Component, Inject, OnInit} from '@angular/core';
import {MAT_SNACK_BAR_DATA, MatSnackBarRef} from '@angular/material/snack-bar';
import {RouterLink} from '@angular/router';
import {BreakpointObserverService, MediaSize} from '@core/services/breakpoint-observer.service';
import {TranslatePipe} from '@ngx-translate/core';

@Component({
    selector: 'app-reset-password-error-box',
    templateUrl: './reset-password-error-box.component.html',
    styleUrls: ['./reset-password-error-box.component.scss'],
    imports: [NgClass, RouterLink, TranslatePipe]
})
export class ResetPasswordErrorBoxComponent implements OnInit {
    /**
     * Pour savoir comment restituer le composant en mode desktop/mobile
     */
    mediaSize: MediaSize;

    constructor(@Inject(MAT_SNACK_BAR_DATA) public data,
                private readonly breakpointObserver: BreakpointObserverService,
                public readonly snackBarRef: MatSnackBarRef<ResetPasswordErrorBoxComponent>) {
    }

    ngOnInit(): void {
        // On récupère les infos sur la restitution
        this.mediaSize = this.breakpointObserver.getMediaSize();
    }

}
