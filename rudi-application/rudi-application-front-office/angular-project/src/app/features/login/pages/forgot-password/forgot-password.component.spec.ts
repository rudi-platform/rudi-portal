import {ComponentFixture, TestBed} from '@angular/core/testing';

import {ForgotPasswordComponent} from './forgot-password.component';

describe('ResetPasswordComponent', () => {
    let component: ForgotPasswordComponent;
    let fixture: ComponentFixture<ForgotPasswordComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [ForgotPasswordComponent]
        })
            .compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(ForgotPasswordComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should expose a level 1 heading and a real sign-up link (RGAA 9.1, 7.1)', () => {
        expect(fixture.nativeElement.querySelectorAll('h1').length).toBe(1);
        const link: HTMLAnchorElement = fixture.nativeElement.querySelector('a.link-bold-underlined-colored');
        expect(link.getAttribute('href')).toBe('/login/sign-up');
    });
});
