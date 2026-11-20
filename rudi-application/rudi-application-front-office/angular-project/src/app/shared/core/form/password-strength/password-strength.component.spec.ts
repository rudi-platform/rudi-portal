import {ComponentFixture, TestBed} from '@angular/core/testing';

import {PasswordStrengthComponent} from '@shared/core/form/password-strength/password-strength.component';

describe('PasswordStrengthComponent', () => {
    let component: PasswordStrengthComponent;
    let fixture: ComponentFixture<PasswordStrengthComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [PasswordStrengthComponent]
        })
            .compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(PasswordStrengthComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should not convey criteria state by color only (RGAA 3.1)', () => {
        fixture.componentRef.setInput('password', 'abc1');
        fixture.detectChanges();

        const criteria: HTMLElement[] = Array.from(fixture.nativeElement.querySelectorAll('.criterion'));
        expect(criteria.map(c => c.querySelector('.visually-hidden').textContent.trim())).toEqual([
            'strengthPassword.accepted', 'strengthPassword.refused', 'strengthPassword.accepted', 'strengthPassword.refused'
        ]);
        expect(criteria.map(c => c.querySelector('.criterion-symbol').getAttribute('data-mat-icon-name'))).toEqual([
            'coche-verte', 'croix-rouge', 'coche-verte', 'croix-rouge'
        ]);
        criteria.forEach(c => expect(c.querySelector('.criterion-symbol').getAttribute('aria-hidden')).toBe('true'));
    });

    it('should hide the decorative strength bars', () => {
        const bars: HTMLElement[] = Array.from(fixture.nativeElement.querySelectorAll('ul.strengthBar'));
        expect(bars.length).toBe(4);
        bars.forEach(bar => expect(bar.getAttribute('aria-hidden')).toBe('true'));
    });

    it('should only accept the special characters allowed by PASSWORD_REGEX', () => {
        const symbolCriterion = [...component.criteria].find(criterion => criterion.i18nKey === 'symbols');
        for (const allowed of ['#', '?', '!', '@', '$', '%', '^', '&', '*', '-']) {
            expect(symbolCriterion.accepts('Abc1' + allowed)).withContext(allowed).toBeTrue();
        }
        for (const refused of ['.', '_', '+', '€', 'é']) {
            expect(symbolCriterion.accepts('Abc1' + refused)).withContext(refused).toBeFalse();
        }
    });
});
