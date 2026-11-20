import {LiveAnnouncer} from '@angular/cdk/a11y';
import {HttpErrorResponse} from '@angular/common/http';
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {MatSnackBar, MatSnackBarConfig} from '@angular/material/snack-bar';
import {Router} from '@angular/router';
import {AccountService} from '@core/services/account.service';
import {ResetPasswordErrorBoxComponent} from '@shared/core/form/reset-password-error-box/reset-password-error-box.component';
import {of, throwError} from 'rxjs';

import {ResetPasswordComponent} from './reset-password.component';

describe('ResetPasswordComponent', () => {
    let component: ResetPasswordComponent;
    let fixture: ComponentFixture<ResetPasswordComponent>;
    let accountService: jasmine.SpyObj<AccountService>;

    beforeEach(async () => {
        accountService = jasmine.createSpyObj('AccountService', {checkPasswordChangeToken: of(true)});
        await TestBed.configureTestingModule({
            imports: [ResetPasswordComponent],
            providers: [
                {provide: AccountService, useValue: accountService}
            ]
        })
            .compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(ResetPasswordComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should announce the expired token error once, through the CDK live region (RGAA 7.5)', () => {
        accountService.checkPasswordChangeToken.and.returnValue(throwError(() => new HttpErrorResponse({status: 404})));
        spyOn(TestBed.inject(Router), 'navigate').and.resolveTo(true);
        const openSpy = spyOn(TestBed.inject(MatSnackBar), 'openFromComponent');
        const announceSpy = spyOn(TestBed.inject(LiveAnnouncer), 'announce').and.resolveTo();

        component.checkPasswordChangeToken();

        const config = openSpy.calls.mostRecent().args[1] as MatSnackBarConfig;
        expect(openSpy.calls.mostRecent().args[0]).toBe(ResetPasswordErrorBoxComponent);
        expect(config.politeness).toBe('off');
        expect(config.duration).withContext('0 = no auto dismiss (RGAA 13.1)').toBe(0);
        expect(announceSpy).toHaveBeenCalledWith('snackbarTemplate.resetPasswordError common.ici', 'assertive');
    });

    it('should describe the password field with its instructions and criteria (RGAA 11.10)', () => {
        const describedBy = fixture.nativeElement.querySelector('#password').getAttribute('aria-describedby').split(' ');
        expect(describedBy).toContain('password-msg');
        expect(describedBy).toContain('password-criteria');
        expect(fixture.nativeElement.querySelector('#password-msg')).not.toBeNull();
        expect(fixture.nativeElement.querySelector('app-password-strength#password-criteria')).not.toBeNull();
    });

    it('should explain a password rejected by the pattern (RGAA 11.10)', () => {
        const password = component.signupForm.get('password');
        password.setValue('Abcdefghijk1.');
        password.markAsTouched();
        fixture.detectChanges();
        const errors = Array.from(fixture.nativeElement.querySelectorAll('mat-form-field:has(#password) mat-error'))
            .map((e: HTMLElement) => e.textContent.trim());
        expect(errors).toEqual(['signup.passwordPatternError']);
    });
});
