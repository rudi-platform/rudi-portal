import {ComponentFixture, TestBed} from '@angular/core/testing';
import {ActivatedRoute} from '@angular/router';
import {AccountService} from '@core/services/account.service';
import {AuthenticationService} from '@core/services/authentication.service';
import {BreakpointObserverService, MediaSize} from '@core/services/breakpoint-observer.service';
import {CaptchaCheckerService} from '@core/services/captcha-checker.service';
import {PropertiesMetierService} from '@core/services/properties-metier.service';
import {RedirectService} from '@core/services/redirect.service';
import {SnackBarService} from '@core/services/snack-bar.service';
import {LoginComponent} from '@features/login/pages/login/login.component';
import {AuthenticatorService} from 'micro_service_modules/acl/acl-api';
import {of} from 'rxjs';

describe('LoginComponent', () => {
    let component: LoginComponent;
    let fixture: ComponentFixture<LoginComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [LoginComponent],
            providers: [
                {provide: ActivatedRoute, useValue: {snapshot: {data: {}, queryParams: {}}}},
                {provide: BreakpointObserverService, useValue: {getMediaSize: () => ({isDeviceDesktop: true} as MediaSize)}},
                {provide: AuthenticatorService, useValue: {getOAuth2Authenticators: () => of([{name: 'rudi'}])}},
                {provide: AuthenticationService, useValue: {}},
                {provide: RedirectService, useValue: {}},
                {provide: SnackBarService, useValue: {}},
                {provide: PropertiesMetierService, useValue: {}},
                {provide: CaptchaCheckerService, useValue: {}},
                {provide: AccountService, useValue: {}}
            ]
        })
            .compileComponents();

        fixture = TestBed.createComponent(LoginComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should expose a real sign-up link (RGAA 7.1)', () => {
        const link: HTMLAnchorElement = fixture.nativeElement.querySelector('a.link-bold-underlined-colored');
        // Relative target cannot resolve against the stubbed ActivatedRoute
        expect(link.hasAttribute('href')).toBeTrue();
    });

    it('should toggle the password visibility with a named native button (RGAA 7.3)', () => {
        const button: HTMLButtonElement = fixture.nativeElement.querySelector('button.visibility-icon');
        const password: HTMLInputElement = fixture.nativeElement.querySelector('input[formcontrolname="password"]');
        expect(button.type).toBe('button');
        expect(button.getAttribute('aria-label')).toBe('common.showPassword');
        expect(button.querySelector('mat-icon').getAttribute('aria-hidden')).toBe('true');
        expect(password.type).toBe('password');

        button.click();
        fixture.detectChanges();

        expect(password.type).toBe('text');
        expect(button.getAttribute('aria-label')).toBe('common.hidePassword');
    });
});
