import {ComponentFixture, TestBed} from '@angular/core/testing';

import {OrganizationLogoComponent} from '@shared/business/organisation/organization-logo/organization-logo.component';

describe('LogoComponent', () => {
    let component: OrganizationLogoComponent;
    let fixture: ComponentFixture<OrganizationLogoComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [OrganizationLogoComponent]
        })
            .compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(OrganizationLogoComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
