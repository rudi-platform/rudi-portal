import {Component, ElementRef, EventEmitter, HostListener, Input, Output} from '@angular/core';
import {AsyncPipe, NgClass} from '@angular/common';
import {Observable} from 'rxjs';
import {MatButton} from '@angular/material/button';
import {MatBadge} from '@angular/material/badge';
import {MatIcon} from '@angular/material/icon';
import {TranslatePipe} from '@ngx-translate/core';

/**
 * Bouton qui affiche/masque un panneau (pattern ARIA Disclosure). Pas un MatMenu exprès : on met des
 * formulaires dedans (cases à cocher, radios), et le clavier d'un menu ARIA ne s'y prête pas - ici le
 * panneau reste un simple conteneur, navigable au Tab normal.
 */
@Component({
    selector: 'app-button-disclosure',
    templateUrl: './button-disclosure.component.html',
    styleUrls: ['./button-disclosure.component.scss'],
    imports: [MatButton, NgClass, MatBadge, MatIcon, AsyncPipe, TranslatePipe]
})
export class ButtonDisclosureComponent {
    private static nextId = 0;

    @Input() buttonTextKey?: string;
    @Input() counter$?: Observable<number>;
    @Input() isHidden = false;
    @Input() triggerClass = '';
    @Input() showStateIcon = true;
    @Input() yPosition: 'above' | 'below' = 'below';

    @Output() closed = new EventEmitter<void>();

    readonly panelId = `button-disclosure-panel-${ButtonDisclosureComponent.nextId++}`;

    private _isOpened = false;

    constructor(private readonly elementRef: ElementRef<HTMLElement>) {
    }

    get isOpened(): boolean {
        return this._isOpened;
    }

    toggle(): void {
        if (this._isOpened) {
            this.close();
        } else {
            this.open();
        }
    }

    open(): void {
        this._isOpened = true;
    }

    /**
     * Ferme le panneau et redonne le focus au bouton, sinon un utilisateur clavier le perd après un
     * Escape ou un clic extérieur. On retrouve le bouton via l'ElementRef plutôt qu'un @ViewChild pour
     * l'avoir dès la création du composant.
     */
    close(): void {
        if (!this._isOpened) {
            return;
        }
        this._isOpened = false;
        this.closed.emit();
        this.elementRef.nativeElement.querySelector<HTMLButtonElement>('button')?.focus();
    }

    @HostListener('document:click', ['$event'])
    onDocumentClick(event: MouseEvent): void {
        if (this._isOpened && !this.elementRef.nativeElement.contains(event.target as Node)) {
            this.close();
        }
    }

    @HostListener('document:keydown.escape')
    onEscape(): void {
        this.close();
    }
}
