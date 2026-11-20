import {Component, viewChild} from '@angular/core';
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {FormControl, Validators} from '@angular/forms';
import {MatStep, MatStepper} from '@angular/material/stepper';
import {TranslateService} from '@ngx-translate/core';
import {SequentialStepperDirective} from './sequential-stepper.directive';

@Component({
    template: `
        <mat-stepper linear appSequentialStepper aria-label="Steps">
            <mat-step label="First" [stepControl]="first"></mat-step>
            <mat-step label="Second"></mat-step>
            <mat-step label="Third"></mat-step>
        </mat-stepper>`,
    imports: [MatStepper, MatStep, SequentialStepperDirective]
})
class HostComponent {
    readonly stepper = viewChild.required(MatStepper);
    readonly first = new FormControl('', Validators.required);
}

describe('SequentialStepperDirective', () => {
    let fixture: ComponentFixture<HostComponent>;
    let element: HTMLElement;

    const headers = (): HTMLElement[] => Array.from(element.querySelectorAll('mat-step-header'));

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [HostComponent]
        }).compileComponents();
        spyOn(TestBed.inject(TranslateService), 'instant').and.callFake(
            (key: string | string[], params?: Record<string, unknown>) => `${key}:${params?.['index']}/${params?.['total']}`
        );

        fixture = TestBed.createComponent(HostComponent);
        element = fixture.nativeElement;
        fixture.detectChanges();
    });

    it('should expose the header as a labelled group of buttons instead of a tab list (RGAA 7.1)', () => {
        const container = element.querySelector('.mat-horizontal-stepper-header-container');

        expect(container.getAttribute('role')).toBe('group');
        expect(container.getAttribute('aria-label')).toBe('Steps');
        expect(container.hasAttribute('aria-orientation')).toBeFalse();
        headers().forEach(header => {
            expect(header.getAttribute('role')).toBe('button');
            ['aria-selected', 'aria-posinset', 'aria-setsize', 'aria-controls'].forEach(name => expect(header.hasAttribute(name)).toBeFalse());
        });
        element.querySelectorAll('.mat-horizontal-stepper-content').forEach(panel => {
            expect(panel.hasAttribute('role')).toBeFalse();
            expect(panel.hasAttribute('aria-labelledby')).toBeFalse();
        });
    });

    it('should describe each step with its position, keep its visible name and mark the current one', () => {
        expect(headers().map(header => header.getAttribute('aria-description'))).toEqual(['stepper.step:1/3', 'stepper.step:2/3', 'stepper.step:3/3']);
        expect(headers().some(header => header.hasAttribute('aria-label'))).toBeFalse();
        expect(headers().map(header => header.getAttribute('aria-current'))).toEqual(['step', null, null]);
    });

    it('should keep Material disabled state, used by the styles, on steps not yet reachable', () => {
        expect(headers().map(header => header.getAttribute('aria-disabled'))).toEqual([null, 'true', 'true']);
    });

    it('should keep the semantics in sync after a selection change', () => {
        fixture.componentInstance.first.setValue('done');
        fixture.componentInstance.stepper().next();
        fixture.detectChanges();

        expect(headers().map(header => header.getAttribute('aria-current'))).toEqual([null, 'step', null]);
        headers().forEach(header => {
            expect(header.getAttribute('role')).toBe('button');
            expect(header.hasAttribute('aria-selected')).toBeFalse();
        });
    });
});
