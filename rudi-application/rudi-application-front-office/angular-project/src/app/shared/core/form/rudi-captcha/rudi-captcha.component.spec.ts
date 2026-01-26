import {ComponentFixture, TestBed} from '@angular/core/testing';

import {RudiCaptchaComponent} from '@shared/core/form/rudi-captcha/rudi-captcha.component';

describe('RudiCaptchaComponent', () => {
    let component: RudiCaptchaComponent;
    let fixture: ComponentFixture<RudiCaptchaComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [RudiCaptchaComponent]
        })
            .compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(RudiCaptchaComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
