import {Clipboard} from '@angular/cdk/clipboard';
import {Component, Input} from '@angular/core';
import {NgIf} from '@angular/common';
import {MatButton} from '@angular/material/button';
import {MatIcon} from '@angular/material/icon';
import {TranslatePipe} from '@ngx-translate/core';


@Component({
    selector: 'app-contact-button',
    templateUrl: './contact-button.component.html',
    styleUrls: ['./contact-button.component.scss'],
    imports: [NgIf, MatButton, MatIcon, TranslatePipe]
})
export class ContactButtonComponent {
    @Input() email: string;
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
