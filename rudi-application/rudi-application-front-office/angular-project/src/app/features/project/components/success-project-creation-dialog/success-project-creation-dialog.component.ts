import {Component} from '@angular/core';
import {FlexModule} from '@angular/flex-layout/flex';
import {MatIconButton} from '@angular/material/button';
import {MatDialogClose, MatDialogContent} from '@angular/material/dialog';
import {MatIcon, MatIconRegistry} from '@angular/material/icon';
import {DomSanitizer} from '@angular/platform-browser';
import {TranslateDirective, TranslatePipe} from '@ngx-translate/core';

@Component({
    selector: 'app-success-project-creation-dialog',
    templateUrl: './success-project-creation-dialog.component.html',
    imports: [MatDialogContent, FlexModule, MatIconButton, MatDialogClose, MatIcon, TranslateDirective, TranslatePipe]
})
export class SuccessProjectCreationDialogComponent {

    constructor(private readonly matIconRegistry: MatIconRegistry,
                private readonly domSanitizer: DomSanitizer
    ) {
        this.matIconRegistry.addSvgIcon(
            'icon-close',
            this.domSanitizer.bypassSecurityTrustResourceUrl('assets/icons/icon-close.svg')
        );
    }
}
