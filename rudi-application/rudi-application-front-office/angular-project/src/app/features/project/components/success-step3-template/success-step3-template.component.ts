import {Component, Input} from '@angular/core';
import {MatButton} from '@angular/material/button';
import {RouterLink} from '@angular/router';

@Component({
    selector: 'app-success-step3-template',
    templateUrl: './success-step3-template.component.html',
    styleUrls: ['./success-step3-template.component.scss'],
    imports: [MatButton, RouterLink]
})
export class SuccessStep3TemplateComponent {

    @Input()
    stepTitle: string;
    @Input()
    stepSubtitle: string;
    @Input()
    stepDescription: string;
    @Input()
    stepDescription2: string;
    @Input()
    buttonTitle: string;
    @Input()
    routerLink: string;
}
