import {afterEveryRender, Directive, ElementRef, inject} from '@angular/core';
import {MatStepper} from '@angular/material/stepper';
import {TranslateService} from '@ngx-translate/core';

const TAB_ONLY_ATTRIBUTES = ['aria-selected', 'aria-posinset', 'aria-setsize', 'aria-controls'];

/**
 * RGAA 7.1: exposes a horizontal MatStepper as a group of sequential steps ("Step 1 of 3" description,
 * aria-current="step") instead of a tab list, without changing its DOM structure, classes or styles.
 */
@Directive({
    selector: 'mat-stepper[appSequentialStepper]',
    standalone: true
})
export class SequentialStepperDirective {

    private readonly stepper = inject(MatStepper);
    private readonly host: HTMLElement = inject(ElementRef).nativeElement;
    private readonly translateService = inject(TranslateService);

    constructor() {
        // Material re-applies its tab ARIA on selection change: normalize (idempotent) after every render
        afterEveryRender({write: () => this.normalize()});
    }

    private normalize(): void {
        const container = this.host.querySelector('.mat-horizontal-stepper-header-container');
        if (!container) {
            return;
        }
        setAttribute(container, 'role', 'group');
        setAttribute(container, 'aria-orientation', null);

        const steps = this.stepper.steps.toArray();
        container.querySelectorAll(':scope > mat-step-header').forEach((header, index) => {
            const step = steps[index];
            setAttribute(header, 'role', 'button');
            TAB_ONLY_ATTRIBUTES.forEach(name => setAttribute(header, name, null));
            if (!step) {
                return;
            }
            setAttribute(header, 'aria-current', step.isSelected() ? 'step' : null);
            // Description, not aria-label: the name must keep the visible label (WCAG 2.5.3)
            setAttribute(header, 'aria-description', this.translateService.instant('stepper.step', {index: index + 1, total: steps.length}));
        });

        this.host.querySelectorAll('.mat-horizontal-stepper-content').forEach(panel => {
            setAttribute(panel, 'role', null);
            setAttribute(panel, 'aria-labelledby', null);
        });
    }
}

function setAttribute(element: Element, name: string, value: string | null): void {
    if (value === null) {
        if (element.hasAttribute(name)) {
            element.removeAttribute(name);
        }
    } else if (element.getAttribute(name) !== value) {
        element.setAttribute(name, value);
    }
}
