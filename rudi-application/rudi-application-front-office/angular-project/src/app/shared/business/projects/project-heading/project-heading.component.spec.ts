import {ComponentFixture, TestBed} from '@angular/core/testing';

import {ProjectHeadingComponent} from './project-heading.component';

describe('ProjectHeadingComponent', () => {
    let fixture: ComponentFixture<ProjectHeadingComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [ProjectHeadingComponent]
        })
            .compileComponents();

        fixture = TestBed.createComponent(ProjectHeadingComponent);
        fixture.componentRef.setInput('logo', 'logo.png');
        fixture.componentRef.setInput('projectTitle', 'Projet');
        fixture.detectChanges();
    });

    it('should render the project title as the page main heading', () => {
        expect(fixture.nativeElement.querySelector('h1')?.textContent).toContain('Projet');
    });

    it('should mark the logo as decorative', () => {
        expect(fixture.nativeElement.querySelector('img').getAttribute('alt')).toBe('');
    });
});
