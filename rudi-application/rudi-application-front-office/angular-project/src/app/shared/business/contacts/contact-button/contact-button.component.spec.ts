import {ComponentFixture, TestBed} from '@angular/core/testing';

import {ContactButtonComponent} from '@shared/business/contacts/contact-button/contact-button.component';

describe('ContactButtonComponent', () => {
    let component: ContactButtonComponent;
    let fixture: ComponentFixture<ContactButtonComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            declarations: [ContactButtonComponent]
        })
            .compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(ContactButtonComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
