import {Component, Input} from '@angular/core';
import {MatButton} from '@angular/material/button';
import {MatIcon, MatIconRegistry} from '@angular/material/icon';
import {DomSanitizer} from '@angular/platform-browser';
import {NgbPopover} from '@ng-bootstrap/ng-bootstrap';

@Component({
    selector: 'app-popover',
    templateUrl: './popover.component.html',
    styleUrls: ['./popover.component.scss'],
    imports: [MatIcon, MatButton, NgbPopover]
})
export class PopoverComponent {
    @Input()
    public buttonLogo: string;
    @Input()
    public buttonMessageBody: string;
    @Input()
    public buttonMessageFooter: string;
    @Input()
    public buttonMessageTitle: string;

    constructor(private readonly matIconRegistry: MatIconRegistry,
                private readonly domSanitizer: DomSanitizer) {
        this.matIconRegistry.addSvgIcon(
            'rudi_picto_reutilisations',
            this.domSanitizer.bypassSecurityTrustResourceUrl('/assets/images/rudi_picto_reutilisations.svg')
        );
        this.matIconRegistry.addSvgIcon(
            'rudi_picto_projet',
            this.domSanitizer.bypassSecurityTrustResourceUrl('/assets/images/rudi_picto_projet.svg')
        );
    }
}
