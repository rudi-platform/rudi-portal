import {Clipboard} from '@angular/cdk/clipboard';
import {Component, Input} from '@angular/core';
import {MatIconButton} from '@angular/material/button';
import {MatIcon} from '@angular/material/icon';

@Component({
    selector: 'app-clipboard-field',
    templateUrl: './clipboard-field.component.html',
    styleUrls: ['clipboard-field.component.scss'],
    imports: [MatIconButton, MatIcon]
})
export class ClipboardFieldComponent {
    @Input()
    content: string;

    constructor(
        private readonly clipboard: Clipboard,
    ) {
        this.content = '';
    }

    copyContentToClipboard(): void {
        this.clipboard.copy(this.content);
    }
}
