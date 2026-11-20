import {ComponentFixture, TestBed} from '@angular/core/testing';
import {ActivatedRoute, provideRouter} from '@angular/router';
import {CustomizationService} from '@app/core/services/customization.service';
import {LogService} from '@app/core/services/log.service';
import {AccountService} from '@core/services/account.service';
import {BreakpointObserverService, MediaSize} from '@core/services/breakpoint-observer.service';
import {CaptchaCheckerService} from '@core/services/captcha-checker.service';
import {PropertiesMetierService} from '@core/services/properties-metier.service';
import {RouteHistoryService} from '@core/services/route-history.service';
import {SnackBarService} from '@core/services/snack-bar.service';
import {provideTranslateService} from '@ngx-translate/core';
import {KonsultService} from 'micro_service_modules/konsult/konsult-api';
import {NEVER} from 'rxjs';

import {SignUpComponent} from './sign-up.component';

describe('SignUpComponent', () => {
    let component: SignUpComponent;
    let fixture: ComponentFixture<SignUpComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [SignUpComponent],
            providers: [
                provideRouter([]),
                provideTranslateService(),
                {provide: ActivatedRoute, useValue: {snapshot: {data: {}}}},
                {provide: BreakpointObserverService, useValue: {getMediaSize: () => ({isDeviceDesktop: true} as MediaSize)}},
                {provide: CustomizationService, useValue: {getCustomizationDescription: () => NEVER}},
                {provide: RouteHistoryService, useValue: {}},
                {provide: SnackBarService, useValue: {}},
                {provide: AccountService, useValue: {}},
                {provide: PropertiesMetierService, useValue: {}},
                {provide: CaptchaCheckerService, useValue: {}},
                {provide: KonsultService, useValue: {}},
                {provide: LogService, useValue: jasmine.createSpyObj('LogService', ['error'])}
            ]
        })
            .compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(SignUpComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should name both checkboxes with their visible label (RGAA 11.1)', () => {
        for (const id of ['cgu-checkbox-input', 'notifications-checkbox-input']) {
            const input: HTMLInputElement = fixture.nativeElement.querySelector(`input[type="checkbox"][id="${id}"]`);
            const label: HTMLLabelElement = fixture.nativeElement.querySelector(`label[for="${id}"]:not(.mdc-label)`);
            expect(input).withContext(id).not.toBeNull();
            expect(Array.from(input.labels)).withContext(id).toContain(label);
        }
    });

    it('should toggle the notifications checkbox when its label is clicked', () => {
        fixture.nativeElement.querySelector('label[for="notifications-checkbox-input"]:not(.mdc-label)').click();
        fixture.detectChanges();
        expect(component.signupForm.get('subscribeToNotifications').value).toBeTrue();
    });

    it('should convey the required state natively and hide the asterisks (RGAA 11.10)', () => {
        const cgu: HTMLInputElement = fixture.nativeElement.querySelector('#cgu-checkbox-input');
        expect(cgu.required).toBeTrue();
        const asterisks: HTMLElement[] = Array.from(fixture.nativeElement.querySelectorAll('form .required-symbol, form mat-label .mat-error'));
        expect(asterisks.length).toBeGreaterThan(0);
        asterisks.forEach(asterisk => expect(asterisk.getAttribute('aria-hidden')).toBe('true'));
    });

    it('should expose a heading hierarchy and a real sign-in link (RGAA 9.1, 7.1)', () => {
        const headings = Array.from(fixture.nativeElement.querySelectorAll('h1, h2, h3')).map((h: HTMLElement) => h.tagName);
        expect(headings).toEqual(['H1', 'H2']);
        const link: HTMLAnchorElement = fixture.nativeElement.querySelector('a.link-bold-underlined-colored');
        expect(link.getAttribute('href')).toBe('/login');
    });

    it('should not submit the form with the password visibility buttons', () => {
        const buttons: HTMLButtonElement[] = Array.from(fixture.nativeElement.querySelectorAll('form button[mat-icon-button]'));
        expect(buttons.length).toBe(2);
        buttons.forEach(button => expect(button.type).toBe('button'));
    });

    it('should hide decorative icons and keep phrasing content in the CGU label (RGAA 1.2, 8.2)', () => {
        const icons: HTMLElement[] = Array.from(fixture.nativeElement.querySelectorAll('em.fa'));
        expect(icons.length).toBeGreaterThan(0);
        icons.forEach(icon => expect(icon.getAttribute('aria-hidden')).toBe('true'));
        expect(fixture.nativeElement.querySelector('label[for="cgu-checkbox-input"] div')).toBeNull();
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
