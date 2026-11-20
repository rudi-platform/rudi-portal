import {ComponentFixture, TestBed} from '@angular/core/testing';
import {provideTranslateService, TranslateService} from '@ngx-translate/core';

import {ProjectBasicDetailsComponent} from './project-basic-details.component';

describe('ProjectBasicDetailsComponent', () => {
    let fixture: ComponentFixture<ProjectBasicDetailsComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [ProjectBasicDetailsComponent],
            providers: [provideTranslateService()]
        })
            .compileComponents();
        const translate = TestBed.inject(TranslateService);
        translate.setTranslation('fr', {personalSpace: {project: {basicInfos: {projectType: 'Type de réutilisation'}}}});
        translate.use('fr');

        fixture = TestBed.createComponent(ProjectBasicDetailsComponent);
        fixture.componentRef.setInput('projectType', 'Application');
        fixture.detectChanges();
    });

    it('should render the project type label as a level 2 heading, as on the public reuse detail (RGAA 9.1)', () => {
        const heading: HTMLElement = fixture.nativeElement.querySelector('h2.card-label');
        expect(heading?.textContent.trim()).toBe('Type de réutilisation');
        expect(heading.nextElementSibling.textContent.trim()).toBe('Application');
    });
});
