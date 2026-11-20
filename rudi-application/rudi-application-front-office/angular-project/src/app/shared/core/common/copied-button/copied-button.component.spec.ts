import {LiveAnnouncer} from '@angular/cdk/a11y';
import {Clipboard} from '@angular/cdk/clipboard';
import {ComponentFixture, TestBed} from '@angular/core/testing';

import {CopiedButtonComponent} from '@shared/core/common/copied-button/copied-button.component';

describe('CopiedButtonComponent', () => {
    let component: CopiedButtonComponent;
    let fixture: ComponentFixture<CopiedButtonComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [CopiedButtonComponent]
        })
            .compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(CopiedButtonComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should expose the value to copy as button text, without nested form field (RGAA 11.1, 11.9)', () => {
        component.toCopy = 'contact@example.org';
        fixture.detectChanges();

        const button: HTMLButtonElement = fixture.nativeElement.querySelector('button.btn-action');
        expect(button.getAttribute('type')).toBe('button');
        expect(button.querySelector('input')).toBeNull();
        expect(button.textContent).toContain('common.copiedButton.copy');
        expect(button.textContent).toContain('contact@example.org');
        expect(button.querySelector('mat-icon').getAttribute('aria-hidden')).toBe('true');
    });

    it('should copy the value and announce it (RGAA 7.5)', () => {
        const clipboard = TestBed.inject(Clipboard);
        const liveAnnouncer = TestBed.inject(LiveAnnouncer);
        spyOn(clipboard, 'copy');
        spyOn(liveAnnouncer, 'announce');
        component.toCopy = 'value';

        fixture.nativeElement.querySelector('button.btn-action').click();
        fixture.detectChanges();

        expect(clipboard.copy).toHaveBeenCalledOnceWith('value');
        expect(liveAnnouncer.announce).toHaveBeenCalledOnceWith('common.copiedButton.copied');
        expect(fixture.nativeElement.querySelector('.copy-icon-button').getAttribute('fontIcon')).toBe('done_all');
    });

    describe('when masked', () => {
        beforeEach(() => {
            fixture = TestBed.createComponent(CopiedButtonComponent);
            component = fixture.componentInstance;
            component.toCopy = 'secret';
            component.masked = true;
            fixture.detectChanges();
        });

        it('should hide the value from display and assistive technologies', () => {
            const button: HTMLButtonElement = fixture.nativeElement.querySelector('button.btn-action');
            expect(button.textContent).not.toContain('secret');
            expect(button.querySelector('.fake-input').getAttribute('aria-hidden')).toBe('true');
            expect(button.textContent).toContain('common.copiedButton.maskedValue');
        });

        it('should toggle the visibility with a named toggle button (RGAA 7.1)', () => {
            const toggle: HTMLButtonElement = fixture.nativeElement.querySelector('button.floating-icon-visibility');
            expect(toggle.getAttribute('type')).toBe('button');
            expect(toggle.getAttribute('aria-label')).toBe('common.copiedButton.showValue');
            expect(toggle.getAttribute('aria-pressed')).toBe('false');

            toggle.click();
            fixture.detectChanges();

            expect(toggle.getAttribute('aria-pressed')).toBe('true');
            expect(fixture.nativeElement.querySelector('button.btn-action').textContent).toContain('secret');
        });

        it('should not overlap the visibility toggle and the copy icon', () => {
            const toggle = fixture.nativeElement.querySelector('button.floating-icon-visibility').getBoundingClientRect();
            const copyIcon = fixture.nativeElement.querySelector('.copy-icon-button').getBoundingClientRect();
            expect(toggle.right).toBeLessThanOrEqual(copyIcon.left);
        });
    });
});
