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

    it('should render an empty alt when the logo is decorative (RGAA 1.2)', () => {
        component.decorative = true;
        fixture.detectChanges();

        expect(fixture.nativeElement.querySelector('img')?.getAttribute('alt')).toBe('');
    });

    it('should keep a text alternative by default', () => {
        component.defaultAlt = 'Logo';
        fixture.detectChanges();

        expect(fixture.nativeElement.querySelector('img')?.getAttribute('alt')).toBe('Logo');
    });
});
