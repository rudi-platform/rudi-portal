import {Component, Input} from '@angular/core';
import {MatLabel} from '@angular/material/form-field';
import {DomSanitizer, SafeHtml} from '@angular/platform-browser';


@Component({
    selector: 'app-project-main-information-rich-label',
    templateUrl: './project-main-information-rich-label.component.html',
    styleUrl: './project-main-information-rich-label.component.scss',
    imports: [MatLabel]
})
export class ProjectMainInformationRichLabelComponent {

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
