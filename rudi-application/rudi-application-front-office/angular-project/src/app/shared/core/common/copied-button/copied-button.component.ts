import {LiveAnnouncer} from '@angular/cdk/a11y';
import {Clipboard} from '@angular/cdk/clipboard';
import {Component, Input, OnInit} from '@angular/core';
import {MatButton} from '@angular/material/button';
import {MatIcon} from '@angular/material/icon';
import {TranslatePipe, TranslateService} from '@ngx-translate/core';

/**
 * Composant génerique du boutton copy avec/sans icon visibilité
 */
@Component({
    selector: 'app-copied-button',
    templateUrl: './copied-button.component.html',
    styleUrls: ['./copied-button.component.scss'],
    imports: [MatButton, MatIcon, TranslatePipe]
})
export class CopiedButtonComponent implements OnInit {

    /**
     * Variable à copier
     */
    @Input() toCopy: string;

    /**
     * affiche ou non le password
     */
    hidePassword: boolean;

    /**
     * affiche l'oeil de visibilité si true ( input de type paswd )
     */
    @Input() masked: boolean;


    /**
     * booléen traduisant l'etat de la copie
     */
    copiedSuccess = false;

    constructor(
        private readonly clipboard: Clipboard,
        private readonly liveAnnouncer: LiveAnnouncer,
        private readonly translateService: TranslateService
    ) {
    }

    ngOnInit(): void {
        this.hidePassword = this.masked;
    }


    /**
     * Méthode qui copie au click du button
     */
    clickOnButton(): void {
        this.copiedSuccess = true;
        this.clipboard.copy(this.toCopy);
        this.liveAnnouncer.announce(this.translateService.instant('common.copiedButton.copied'));
    }

    /**
     * Valeur masquée affichée à la place de la valeur à copier
     */
    getMaskedValue(): string {
        return '•'.repeat(this.toCopy?.length ?? 0);
    }

    /**
     * Affiche le contenu de l'input en TITLE uniquement quand la valeur à copier est affichée en clair
     */
    getInputTitle(): string {
        if (!this.hidePassword) {
            return this.toCopy;
        }

        return '';
    }
}
