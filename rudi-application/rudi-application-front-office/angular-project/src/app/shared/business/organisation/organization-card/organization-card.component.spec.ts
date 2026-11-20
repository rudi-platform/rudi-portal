import {ComponentFixture, TestBed} from '@angular/core/testing';
import {By} from '@angular/platform-browser';
import {URIComponentCodec} from '@core/services/codecs/uri-component-codec';
import {OrganizationBean} from 'micro_service_modules/strukture/api-strukture';
import {OrganizationLogoComponent} from '../organization-logo/organization-logo.component';
import {OrganizationCardComponent} from './organization-card.component';

describe('OrganizationCardComponent', () => {
    let fixture: ComponentFixture<OrganizationCardComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [OrganizationCardComponent],
            providers: [URIComponentCodec]
        }).compileComponents();

        fixture = TestBed.createComponent(OrganizationCardComponent);
        fixture.componentInstance.organizationBean = {
            uuid: 'a', name: 'Org A', description: 'Desc', datasetCount: 3, projectCount: 1
        } as OrganizationBean;
        fixture.detectChanges();
    });

    it('should render the organization name as a heading (RGAA 9.1)', () => {
        const heading: HTMLElement = fixture.nativeElement.querySelector('h2.resource-title');

        expect(heading?.textContent).toContain('Org A');
    });

    it('should render the organization name as a level 3 heading when requested (RGAA 9.1)', () => {
        fixture.componentRef.setInput('titleLevel', 3);
        fixture.detectChanges();
        const element: HTMLElement = fixture.nativeElement;

        expect(element.querySelector('h3.resource-title')?.textContent).toContain('Org A');
        expect(element.querySelector('h2')).toBeNull();
    });

    it('should render the counters as list items, not buttons (RGAA 7.1)', () => {
        const element: HTMLElement = fixture.nativeElement;

        expect(element.querySelector('button')).toBeNull();
        expect(element.querySelectorAll('ul.organization-card-actions > li').length).toBe(2);
    });

    it('should render the logo as decorative (RGAA 1.2)', () => {
        const logo = fixture.debugElement.query(By.directive(OrganizationLogoComponent)).componentInstance as OrganizationLogoComponent;

        expect(logo.decorative).toBeTrue();
    });
});
