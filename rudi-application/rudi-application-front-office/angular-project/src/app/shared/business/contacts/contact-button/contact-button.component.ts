import {Clipboard} from '@angular/cdk/clipboard';
import {Component, Input} from '@angular/core';

import {MatButton} from '@angular/material/button';
import {MatIcon} from '@angular/material/icon';
import {TranslatePipe} from '@ngx-translate/core';


@Component({
    selector: 'app-contact-button',
    templateUrl: './contact-button.component.html',
    styleUrls: ['./contact-button.component.scss'],
    imports: [MatButton, MatIcon, TranslatePipe]
})
export class ContactButtonComponent {
    @Input() email: string;

    /**
     * Si true, l'email est visible directement (sans bouton "Contact").
     * Par défaut false pour conserver le comportement existant.
     */
    @Input()
    set showEmailDirectly(value: boolean) {
        this._showEmailDirectly = value;
        this.emailIsVisible = value;
    }
    get showEmailDirectly(): boolean {
        return this._showEmailDirectly;
    }

    private _showEmailDirectly = false;

    /**
     * Libellé affiché avant révélation de l'email.
     * Si non renseigné, on utilise la traduction 'metaData.contact'.
     */
    @Input() label?: string;
    emailIsVisible = false;
    copiedSuccess = false;


    constructor(private readonly clipboard: Clipboard) {
    }

    clickContactButton(): void {
        this.emailIsVisible = true;
    }

    clickEmailButton(): void {
        this.copiedSuccess = true;
        this.clipboard.copy(this.email);
    }
}
