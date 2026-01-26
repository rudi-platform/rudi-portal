import {Component} from '@angular/core';
import {MatIconRegistry, MatIcon} from '@angular/material/icon';
import {DomSanitizer} from '@angular/platform-browser';
import {CdkScrollable} from '@angular/cdk/scrolling';
import {MatDialogContent, MatDialogClose} from '@angular/material/dialog';
import {FlexModule} from '@angular/flex-layout/flex';
import {MatIconButton} from '@angular/material/button';
import {TranslateDirective, TranslatePipe} from '@ngx-translate/core';

@Component({
    selector: 'app-success-project-creation-dialog',
    templateUrl: './success-project-creation-dialog.component.html',
    imports: [CdkScrollable, MatDialogContent, FlexModule, MatIconButton, MatDialogClose, MatIcon, TranslateDirective, TranslatePipe]
})
export class SuccessProjectCreationDialogComponent {

    constructor(private matIconRegistry: MatIconRegistry,
                private domSanitizer: DomSanitizer
    ) {
        this.matIconRegistry.addSvgIcon(
            'icon-close',
            this.domSanitizer.bypassSecurityTrustResourceUrl('assets/icons/icon-close.svg')
        );
    }
}
