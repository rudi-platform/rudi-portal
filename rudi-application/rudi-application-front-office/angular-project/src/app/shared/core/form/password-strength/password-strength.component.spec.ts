import {ComponentFixture, TestBed} from '@angular/core/testing';

import {PasswordStrengthComponent} from '@shared/core/form/password-strength/password-strength.component';

describe('PasswordStrengthComponent', () => {
    let component: PasswordStrengthComponent;
    let fixture: ComponentFixture<PasswordStrengthComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            declarations: [PasswordStrengthComponent]
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
});
