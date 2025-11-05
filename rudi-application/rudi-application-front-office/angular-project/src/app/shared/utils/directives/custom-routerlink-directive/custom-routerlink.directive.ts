import {Directive, ElementRef} from '@angular/core';

@Directive({
    selector: '[routerLink]',
    standalone: false
})
export class CustomRouterlinkDirective {

    constructor(el: ElementRef) {
        el.nativeElement.classList.add('pointer');
    }
}
