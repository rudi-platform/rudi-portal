import {Directive, ViewContainerRef} from '@angular/core';

@Directive({
    selector: '[appTabContent]',
    standalone: false
})
export class TabContentDirective {

    constructor(
        readonly viewContainer: ViewContainerRef,
    ) {
    }

}
