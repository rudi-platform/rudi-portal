import {ComponentFixture, TestBed} from '@angular/core/testing';
import {TruncatedTextComponent} from './truncated-text.component';

describe('TruncatedTextComponent', () => {
    let fixture: ComponentFixture<TruncatedTextComponent>;

    const render = (focusable: boolean | undefined, overflowing: boolean): HTMLElement => {
        fixture = TestBed.createComponent(TruncatedTextComponent);
        fixture.componentRef.setInput('text', 'Full text');
        if (focusable !== undefined) {
            fixture.componentRef.setInput('focusable', focusable);
        }
        fixture.detectChanges();
        // Force the measured state: layout is not reliable in Karma.
        (fixture.componentInstance as unknown as { isOverflowing: boolean }).isOverflowing = overflowing;
        fixture.componentRef.changeDetectorRef.detectChanges();
        return fixture.nativeElement as HTMLElement;
    };

    beforeEach(async () => {
        await TestBed.configureTestingModule({imports: [TruncatedTextComponent]}).compileComponents();
    });

    it('keeps the truncated text focusable by default (keyboard access to the tooltip)', () => {
        const host = render(undefined, true);
        expect(host.querySelector('.truncated-text')?.getAttribute('tabindex')).toBe('0');
        expect(host.querySelector('.visually-hidden')).toBeNull();
    });

    it('is not a tab stop when the text is not truncated', () => {
        const host = render(undefined, false);
        expect(host.querySelector('.truncated-text')?.hasAttribute('tabindex')).toBeFalse();
    });

    it('exposes the full text to screen readers without tab stop when focusable is false (RGAA 10.13, 12.8)', () => {
        const host = render(false, true);
        const visible = host.querySelector('.truncated-text');
        expect(visible?.hasAttribute('tabindex')).toBeFalse();
        expect(visible?.getAttribute('aria-hidden')).toBe('true');
        expect(host.querySelector('.visually-hidden')?.textContent).toBe('Full text');
    });

    it('renders no hidden copy when focusable is false and the text fits', () => {
        const host = render(false, false);
        expect(host.querySelector('.truncated-text')?.hasAttribute('aria-hidden')).toBeFalse();
        expect(host.querySelector('.visually-hidden')).toBeNull();
    });
});
