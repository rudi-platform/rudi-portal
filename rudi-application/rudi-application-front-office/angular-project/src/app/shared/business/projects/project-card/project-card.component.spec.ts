import {ComponentFixture, TestBed} from '@angular/core/testing';
import {ProjectCatalogItem} from '@features/project/model/project-catalog-item';

import {ProjectCardComponent} from '@shared/business/projects/project-card/project-card.component';

describe('ProjectCardComponent', () => {
    let component: ProjectCardComponent;
    let fixture: ComponentFixture<ProjectCardComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [ProjectCardComponent]
        })
            .compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(ProjectCardComponent);
        component = fixture.componentInstance;
        fixture.componentRef.setInput('projectCatalogItem', {
            logo: 'logo.png',
            project: {uuid: 'project-uuid', title: 'Projet'}
        } as ProjectCatalogItem);
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should render the title as a level 2 heading by default', () => {
        const element: HTMLElement = fixture.nativeElement;
        expect(element.querySelector('h2.project-card-heading')?.textContent).toContain('Projet');
        expect(element.querySelector('h3')).toBeNull();
    });

    it('should render the title as a level 3 heading when requested', () => {
        fixture.componentRef.setInput('titleLevel', 3);
        fixture.detectChanges();
        const element: HTMLElement = fixture.nativeElement;
        expect(element.querySelector('h3.project-card-heading')?.textContent).toContain('Projet');
        expect(element.querySelector('h2')).toBeNull();
    });

    it('should mark the picture as decorative', () => {
        expect(fixture.nativeElement.querySelector('img').getAttribute('alt')).toBe('');
    });

    it('should expose a single tab stop, the stretched link named after the project', () => {
        const element: HTMLElement = fixture.nativeElement;
        expect(element.querySelector('a.stretched-link')?.getAttribute('aria-label')).toBe('Projet');
        expect(element.querySelectorAll('[tabindex="0"]').length).toBe(0);
    });
});
