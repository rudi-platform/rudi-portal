import {ComponentFixture, TestBed} from '@angular/core/testing';
import {Project} from 'micro_service_modules/projekt/projekt-model';

import {DetailComponent} from './detail.component';

describe('DetailComponent', () => {
    let component: DetailComponent;
    let fixture: ComponentFixture<DetailComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [DetailComponent]
        })
            .compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(DetailComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should expose the access URL as a native link opening a new window', () => {
        component.project = {
            uuid: 'p',
            title: 'Projet',
            access_url: 'https://example.org/',
            reutilisation_status: {label: 'Statut'}
        } as Project;
        fixture.detectChanges();

        const link: HTMLAnchorElement = fixture.nativeElement.querySelector('a.bandeau-btn-access');
        expect(link.getAttribute('href')).toBe('https://example.org/');
        expect(link.getAttribute('target')).toBe('_blank');
        expect(link.getAttribute('rel')).toBe('noopener noreferrer');
        expect(link.querySelector('.visually-hidden')).not.toBeNull();
        expect(fixture.nativeElement.querySelector('h1')?.textContent).toContain('Projet');
    });

    it('should expose the informations tab like the organization detail page', () => {
        component.project = {uuid: 'p', title: 'Projet', reutilisation_status: {label: 'Statut'}} as Project;
        fixture.detectChanges();

        const tab: HTMLButtonElement = fixture.nativeElement.querySelector('[role="tablist"] [role="tab"]');
        const panel: HTMLElement = fixture.nativeElement.querySelector('[role="tabpanel"]');
        expect(tab.disabled).toBeFalse();
        expect(tab.getAttribute('aria-selected')).toBe('true');
        expect(tab.getAttribute('aria-controls')).toBe(panel.id);
        expect(panel.getAttribute('aria-labelledby')).toBe(tab.id);
    });
});
