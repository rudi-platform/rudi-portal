import {Component, Input} from '@angular/core';
import {DomSanitizer, SafeHtml} from '@angular/platform-browser';
import { MatLabel } from '@angular/material/form-field';
import { NgIf } from '@angular/common';

@Component({
    selector: 'app-worklfow-expansion-rich-label',
    templateUrl: './worklfow-expansion-rich-label.component.html',
    styleUrl: './worklfow-expansion-rich-label.component.scss',
    imports: [MatLabel, NgIf]
})
export class WorklfowExpansionRichLabelComponent {
    @Input() label: string;
    @Input() value: any;

    constructor(private domSanitizer: DomSanitizer,) {
    }

    get safeValue(): SafeHtml {
        return this.value ? this.sanitize(this.value) : '';
    }

    sanitize(html: string): SafeHtml {
        return this.domSanitizer.bypassSecurityTrustHtml(html);
    }
}
