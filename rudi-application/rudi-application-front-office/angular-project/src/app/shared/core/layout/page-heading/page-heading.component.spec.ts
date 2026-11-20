import {CUSTOM_ELEMENTS_SCHEMA} from '@angular/core';
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {OrganizationLogoComponent} from '@shared/business/organisation/organization-logo/organization-logo.component';
import {PageHeadingComponent} from '@shared/core/layout/page-heading/page-heading.component';

describe('PageHeadingComponent', () => {
    let fixture: ComponentFixture<PageHeadingComponent>;
    let element: HTMLElement;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [PageHeadingComponent]
        })
            .overrideComponent(PageHeadingComponent, {
                remove: {imports: [OrganizationLogoComponent]},
                add: {schemas: [CUSTOM_ELEMENTS_SCHEMA]}
            })
            .compileComponents();

        fixture = TestBed.createComponent(PageHeadingComponent);
        element = fixture.nativeElement;
    });

    it('should mark the logo as decorative when the title is displayed next to it (RGAA 1.2)', () => {
        fixture.componentRef.setInput('resourceTitle', 'Qualité de l\'air');
        fixture.detectChanges();

        const logo = element.querySelector('app-organization-logo') as HTMLElement & { decorative: boolean };
        expect(logo.decorative).toBeTrue();
    });

    it('should expose the title icon meaning to assistive technologies and mouse users (RGAA 1.1 / 1.3)', () => {
        fixture.componentRef.setInput('resourceTitle', 'Qualité de l\'air');
        fixture.componentRef.setInput('icon', 'key_icon_88_secondary-color');
        fixture.componentRef.setInput('iconAlt', 'Données à accès restreint');
        fixture.detectChanges();

        const icon = element.querySelector('h1 mat-icon');
        expect(icon?.getAttribute('role')).toBe('img');
        expect(icon?.getAttribute('aria-hidden')).toBe('false');
        expect(icon?.getAttribute('aria-label')).toBe('Données à accès restreint');
        expect(icon?.getAttribute('title')).toBe('Données à accès restreint');
        expect(element.querySelector('h1 .visually-hidden')).toBeNull();
    });

    it('should keep the title icon decorative without iconAlt (RGAA 1.2)', () => {
        fixture.componentRef.setInput('resourceTitle', 'Qualité de l\'air');
        fixture.componentRef.setInput('icon', 'key_icon_88_secondary-color');
        fixture.detectChanges();

        const icon = element.querySelector('h1 mat-icon');
        expect(icon?.getAttribute('aria-hidden')).toBe('true');
        expect(icon?.hasAttribute('aria-label')).toBeFalse();
        expect(icon?.hasAttribute('title')).toBeFalse();
    });
});
