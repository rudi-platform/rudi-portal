import {inject, ViewContainerRef} from '@angular/core';
import {TabContentDirective} from '@shared/utils/directives/tab-content-directive/tab-content.directive';

describe('TabContentDirective', () => {
    it('should create an instance', () => {
        const viewContainerRef = inject(ViewContainerRef);
        const directive = new TabContentDirective(viewContainerRef);
        expect(directive).toBeTruthy();
    });
});
