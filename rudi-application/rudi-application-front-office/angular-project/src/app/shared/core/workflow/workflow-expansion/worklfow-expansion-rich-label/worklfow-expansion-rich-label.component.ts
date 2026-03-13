import {Component, Input} from '@angular/core';
import {MatLabel} from '@angular/material/form-field';
import {DomSanitizer, SafeHtml} from '@angular/platform-browser';

@Component({
    selector: 'app-worklfow-expansion-rich-label',
    templateUrl: './worklfow-expansion-rich-label.component.html',
    styleUrl: './worklfow-expansion-rich-label.component.scss',
    imports: [MatLabel]
})
export class WorklfowExpansionRichLabelComponent {
    @Input() label: string;
    @Input() value: any;

    constructor(private readonly domSanitizer: DomSanitizer,) {
    }

    get safeValue(): SafeHtml {
        return this.value ? this.sanitize(this.value) : '';
    }

    sanitize(html: string): SafeHtml {
        return this.domSanitizer.bypassSecurityTrustHtml(html);
    }
}
