import {ComponentFixture, TestBed} from '@angular/core/testing';
import {OrganizationOrderComponent} from './organization-order.component';

describe('OrganizationOrderComponent', () => {
    let fixture: ComponentFixture<OrganizationOrderComponent>;

    beforeEach(() => {
        TestBed.configureTestingModule({imports: [OrganizationOrderComponent]});
        fixture = TestBed.createComponent(OrganizationOrderComponent);
        fixture.detectChanges();
    });

    it('should not expose the arrow ligature name as text (RGAA 10.3)', () => {
        const icon: HTMLElement = fixture.nativeElement.querySelector('mat-icon.rudi-select-arrow');
        const trigger: HTMLElement = fixture.nativeElement.querySelector('button');

        expect(icon.textContent.trim()).toBe('');
        expect(icon.getAttribute('fontIcon') ?? icon.getAttribute('data-mat-icon-name')).toBe('keyboard_arrow_down');
        expect(trigger.textContent).not.toContain('keyboard_arrow');

        fixture.componentInstance.toggleMenu();
        fixture.detectChanges();

        expect(icon.getAttribute('fontIcon') ?? icon.getAttribute('data-mat-icon-name')).toBe('keyboard_arrow_up');
    });
});
