import {ComponentFixture, fakeAsync, TestBed, tick} from '@angular/core/testing';

import {PopoverComponent} from '@shared/core/layout/popover/popover.component';

describe('PopoverComponent', () => {
    let component: PopoverComponent;
    let fixture: ComponentFixture<PopoverComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [PopoverComponent]
        })
            .compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(PopoverComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    describe('additional content (RGAA 10.13)', () => {
        const trigger = (): HTMLButtonElement => fixture.nativeElement.querySelector('button');
        const popoverWindow = (): HTMLElement | null => fixture.nativeElement.querySelector('ngb-popover-window');

        beforeEach(() => {
            component.buttonMessageBody = 'Body';
            component.buttonMessageFooter = 'Footer';
            fixture.detectChanges();
        });

        it('should open on keyboard focus and describe the button with the popover content', () => {
            trigger().dispatchEvent(new FocusEvent('focusin'));
            fixture.detectChanges();

            expect(popoverWindow()?.textContent).toContain('Body');
            expect(trigger().getAttribute('aria-describedby')).toBe(popoverWindow()?.id);
        });

        it('should close with Escape without moving the focus', fakeAsync(() => {
            trigger().dispatchEvent(new FocusEvent('focusin'));
            fixture.detectChanges();

            document.dispatchEvent(new KeyboardEvent('keydown', {key: 'Escape'}));
            fixture.detectChanges();
            tick();

            expect(trigger().hasAttribute('aria-describedby')).toBeFalse();
        }));

        it('should stay open while the pointer moves from the button onto the popover', fakeAsync(() => {
            trigger().dispatchEvent(new MouseEvent('mouseenter'));
            fixture.detectChanges();
            trigger().dispatchEvent(new MouseEvent('mouseleave'));
            popoverWindow()?.dispatchEvent(new MouseEvent('mouseenter'));
            tick(component.popoverCloseDelayMs + 50);
            fixture.detectChanges();

            expect(trigger().hasAttribute('aria-describedby')).toBeTrue();

            popoverWindow()?.dispatchEvent(new MouseEvent('mouseleave'));
            tick(component.popoverCloseDelayMs + 50);

            expect(trigger().hasAttribute('aria-describedby')).toBeFalse();
        }));

        it('should not expose the decorative icon to assistive technologies', () => {
            trigger().dispatchEvent(new FocusEvent('focusin'));
            fixture.detectChanges();

            const icon = popoverWindow()?.querySelector('mat-icon');
            expect(icon?.getAttribute('aria-hidden')).toBe('true');
            expect(icon?.hasAttribute('aria-label')).toBeFalse();
        });
    });
});
