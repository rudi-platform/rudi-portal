import {ComponentFixture, TestBed} from '@angular/core/testing';
import {MAT_SNACK_BAR_DATA, MatSnackBarRef} from '@angular/material/snack-bar';

import {ResetPasswordErrorBoxComponent} from '@shared/core/form/reset-password-error-box/reset-password-error-box.component';

describe('ResetPasswordErrorBoxComponent', () => {
    let component: ResetPasswordErrorBoxComponent;
    let fixture: ComponentFixture<ResetPasswordErrorBoxComponent>;
    let snackBarRef: jasmine.SpyObj<MatSnackBarRef<ResetPasswordErrorBoxComponent>>;

    beforeEach(async () => {
        snackBarRef = jasmine.createSpyObj('MatSnackBarRef', ['dismiss']);
        await TestBed.configureTestingModule({
            imports: [ResetPasswordErrorBoxComponent],
            providers: [
                {provide: MAT_SNACK_BAR_DATA, useValue: {}},
                {provide: MatSnackBarRef, useValue: snackBarRef},
            ]
        })
            .compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(ResetPasswordErrorBoxComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should hide the decorative icons (RGAA 1.2)', () => {
        const icons: HTMLElement[] = Array.from(fixture.nativeElement.querySelectorAll('em'));
        expect(icons.length).toBe(2);
        icons.forEach(icon => expect(icon.getAttribute('aria-hidden')).withContext(icon.className).toBe('true'));
    });

    it('should close through a named native button (RGAA 7.3 / 11.9)', () => {
        const button: HTMLButtonElement = fixture.nativeElement.querySelector('button');
        expect(button.type).toBe('button');
        expect(button.getAttribute('aria-label')).toBe('popup.close');
        button.click();
        expect(snackBarRef.dismiss).toHaveBeenCalled();
    });

    it('should offer a keyboard reachable link to the forgot password page (RGAA 7.3)', () => {
        const link: HTMLAnchorElement = fixture.nativeElement.querySelector('a');
        expect(link.getAttribute('href')).toBe('/login/forgot-password');
        link.dispatchEvent(new MouseEvent('click', {button: 0, cancelable: true}));
        expect(snackBarRef.dismiss).toHaveBeenCalled();
    });
});
