import {ComponentFixture, TestBed} from '@angular/core/testing';
import {provideRouter} from '@angular/router';
import {ProjectDependenciesFetchers, ProjectDependenciesService} from '@core/services/asset/project/project-dependencies.service';
import {ProjektMetierService} from '@core/services/asset/project/projekt-metier.service';
import {UserService} from '@core/services/user.service';
import {provideTranslateService, TranslateService} from '@ngx-translate/core';
import {NEVER} from 'rxjs';

import {ProjectSummary, ReusesComponent} from './reuses.component';

describe('ReusesComponent', () => {
    let component: ReusesComponent;
    let fixture: ComponentFixture<ReusesComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [ReusesComponent],
            providers: [
                provideRouter([]),
                provideTranslateService(),
                {provide: ProjektMetierService, useValue: {getMyAndOrganizationsProjects: () => NEVER}},
                {provide: ProjectDependenciesService, useValue: {searchMyProjects: () => NEVER}},
                {provide: ProjectDependenciesFetchers, useValue: {}},
                {provide: UserService, useValue: {}}
            ]
        })
            .compileComponents();
        const translate = TestBed.inject(TranslateService);
        translate.setTranslation('fr', {
            common: {sortBy: 'Trier par {{column}}'},
            personalSpace: {myReuses: {updatedDate: 'Date de mise à jour', resultTilte: 'réutilisation(s)'}}
        });
        translate.use('fr');
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(ReusesComponent);
        component = fixture.componentInstance;
        fixture.componentRef.setInput('ITEMS_PER_PAGE', 10);
        fixture.detectChanges();
        component.total = 1;
        component.projectList = [{uuid: 'project-uuid', updatedDate: new Date(2026, 10, 5), projectTitle: 'Réutilisation LBO'} as ProjectSummary];
        component.searchIsRunning = false;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should expose each reuse through a link named by its title (RGAA 6.1, 7.3)', () => {
        const link: HTMLAnchorElement = fixture.nativeElement.querySelector('tr.mat-mdc-row td.dataset-title a');
        expect(link.textContent.trim()).toBe('Réutilisation LBO');
        expect(link.getAttribute('href')).toBe('/my-project-details/project-uuid');
    });

    it('should not make the rows themselves focusable', () => {
        const row: HTMLElement = fixture.nativeElement.querySelector('tr.mat-mdc-row');
        expect(row.hasAttribute('tabindex')).toBeFalse();
    });

    it('should name the table with a hidden caption and display the count as plain text (RGAA 5.4)', () => {
        const count: HTMLElement = fixture.nativeElement.querySelector('app-search-count');
        const caption: HTMLElement = fixture.nativeElement.querySelector('table caption');
        expect(caption.textContent.trim()).toBe('1 réutilisation(s)');
        expect(caption.classList).toContain('visually-hidden');
        expect(count.textContent.trim()).toBe('1 réutilisation(s)');
        expect(count.hasAttribute('tabindex')).toBeFalse();
        expect(count.querySelector('a')).toBeNull();
    });

    it('should describe the sort action in French (RGAA 8.7)', () => {
        const sortButton: HTMLElement = fixture.nativeElement.querySelector('th.mat-column-updatedDate .mat-sort-header-container');
        const description = document.getElementById(sortButton.getAttribute('aria-describedby'));
        expect(description.textContent).toBe('Trier par Date de mise à jour');
    });
});
