import {LiveAnnouncer} from '@angular/cdk/a11y';
import {Clipboard} from '@angular/cdk/clipboard';
import {Component, ElementRef, Input, ViewChild} from '@angular/core';

import {MatButton} from '@angular/material/button';
import {MatIcon} from '@angular/material/icon';
import {TranslatePipe, TranslateService} from '@ngx-translate/core';


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

    private focusEmailButtonOnRender = false;

    // The contact button is removed from the DOM when clicked: keep the keyboard focus on its replacement.
    @ViewChild('emailButton', {read: ElementRef})
    set emailButton(button: ElementRef<HTMLButtonElement> | undefined) {
        if (button && this.focusEmailButtonOnRender) {
            this.focusEmailButtonOnRender = false;
            button.nativeElement.focus();
        }
    }

    constructor(private readonly clipboard: Clipboard,
                private readonly liveAnnouncer: LiveAnnouncer,
                private readonly translateService: TranslateService) {
    }

    clickContactButton(): void {
        this.focusEmailButtonOnRender = true;
        this.emailIsVisible = true;
    }

    clickEmailButton(): void {
        this.copiedSuccess = true;
        this.clipboard.copy(this.email);
        this.liveAnnouncer.announce(this.translateService.instant('common.copiedButton.copied'));
    }
}
