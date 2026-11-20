import {ComponentFixture, TestBed} from '@angular/core/testing';

import {MemberPopinComponent} from '@shared/business/organisation/member-popin/member-popin.component';

describe('MemberPopinComponent', () => {
    let component: MemberPopinComponent;
    let fixture: ComponentFixture<MemberPopinComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [MemberPopinComponent]
        })
            .compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(MemberPopinComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should give the close button an accessible name (RGAA 11.9)', () => {
        const button: HTMLButtonElement = fixture.nativeElement.querySelector('.dialog-cross-button button');
        expect(button.getAttribute('type')).toBe('button');
        expect(button.getAttribute('aria-label')).toBe('popup.close');
        expect(button.querySelector('mat-icon').getAttribute('aria-hidden')).toBe('true');
    });

    it('should name the dialog by its visible title (RGAA 7.1)', () => {
        const title: HTMLElement = fixture.nativeElement.querySelector('.dialog-title');
        expect(title.classList).toContain('mat-mdc-dialog-title');
        expect(title.id).toMatch(/^mat-mdc-dialog-title-/);
    });

    it('should name the role select by its visible label, not by its value (RGAA 11.1)', () => {
        const select: HTMLElement = fixture.nativeElement.querySelector('mat-select#member-role');
        const label: HTMLElement = fixture.nativeElement.querySelector('#' + select.getAttribute('aria-labelledby').trim());
        expect(label.tagName).toBe('LABEL');
        expect(label.textContent).toContain('metaData.administrationTab.membersTable.updatePopin.role');
    });

    it('should convey the required state natively and hide the field asterisks (RGAA 11.10)', () => {
        const asterisks: HTMLElement[] = Array.from(fixture.nativeElement.querySelectorAll('label .required-symbol'));
        expect(asterisks.length).toBe(2);
        asterisks.forEach(asterisk => {
            expect(asterisk.getAttribute('aria-hidden')).toBe('true');
            expect(asterisk.hasAttribute('aria-label')).toBeFalse();
        });
        expect(fixture.nativeElement.querySelector('#member-login').hasAttribute('required')).toBeTrue();
        expect(fixture.nativeElement.querySelector('#member-role').getAttribute('aria-required')).toBe('true');
    });
});
