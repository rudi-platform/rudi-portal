import {ComponentFixture, TestBed} from '@angular/core/testing';

import {ErrorBoxComponent} from './error-box.component';

describe('ErrorBoxComponent', () => {
    let component: ErrorBoxComponent;
    let fixture: ComponentFixture<ErrorBoxComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [ErrorBoxComponent]
        })
            .compileComponents();

        fixture = TestBed.createComponent(ErrorBoxComponent);
        component = fixture.componentInstance;
        fixture.componentRef.setInput('text', 'Error message.');
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should announce the error as soon as it is displayed (RGAA 7.5)', () => {
        const zone: HTMLElement = fixture.nativeElement.querySelector('.error-zone');
        expect(zone.getAttribute('role')).toBe('alert');
        expect(zone.textContent.trim()).toBe('Error message.');
    });

    it('should hide the decorative icon without rendering its ligature as text (RGAA 1.2 / 10.2)', () => {
        const icon: HTMLElement = fixture.nativeElement.querySelector('mat-icon');
        expect(icon.getAttribute('aria-hidden')).toBe('true');
        expect(icon.getAttribute('fontIcon')).toBe('error');
        expect(icon.textContent.trim()).toBe('');
    });

    it('should not render any action without clickable text', () => {
        expect(fixture.nativeElement.querySelector('.error-clickable')).toBeNull();
    });

    it('should expose the clickable text as a native button (RGAA 7.1 / 7.3)', () => {
        const clicked = spyOn(component.clickableTextClicked, 'emit');
        fixture.componentRef.setInput('clickableText', 'sign up');
        fixture.detectChanges();

        const button: HTMLButtonElement = fixture.nativeElement.querySelector('.error-clickable');
        expect(button.tagName).toBe('BUTTON');
        expect(button.getAttribute('type')).toBe('button');
        expect(button.textContent.trim()).toBe('sign up');

        button.click();
        expect(clicked).toHaveBeenCalledTimes(1);
    });
});
