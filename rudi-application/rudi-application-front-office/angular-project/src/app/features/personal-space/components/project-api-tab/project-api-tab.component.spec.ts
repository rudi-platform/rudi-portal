import {ComponentFixture, TestBed} from '@angular/core/testing';
import {MatDialog} from '@angular/material/dialog';
import {ActivatedRoute} from '@angular/router';
import {ProjectDependenciesService} from '@core/services/asset/project/project-dependencies.service';
import {LogService} from '@core/services/log.service';
import {PropertiesMetierService} from '@core/services/properties-metier.service';
import {provideTranslateService, TranslateService} from '@ngx-translate/core';
import {ProjektService} from 'micro_service_modules/projekt/projekt-api';
import {NEVER, of} from 'rxjs';

import {ProjectApiTabComponent} from './project-api-tab.component';

describe('ProjectApiTabComponent', () => {
    let fixture: ComponentFixture<ProjectApiTabComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [ProjectApiTabComponent],
            providers: [
                provideTranslateService(),
                {provide: ActivatedRoute, useValue: {params: of({projectUuid: 'project-uuid'})}},
                {provide: ProjectDependenciesService, useValue: {getProject: () => NEVER}},
                {provide: PropertiesMetierService, useValue: {get: () => of('https://doc.rudi.fr/')}},
                {
                    provide: ProjektService, useValue: {
                        searchProjectKeys: () => of({
                            total: 1,
                            elements: [{uuid: 'key-uuid', name: 'LBO_KEY', creationDate: '2026-11-05', expirationDate: '2027-11-05'}]
                        })
                    }
                },
                {provide: MatDialog, useValue: {}},
                {provide: LogService, useValue: jasmine.createSpyObj('LogService', ['error'])}
            ]
        })
            .compileComponents();
        const translate = TestBed.inject(TranslateService);
        translate.setTranslation('fr', {
            footer: {newWindow: '(nouvelle fenêtre)'},
            personalSpace: {
                projectApi: {identificationKey: 'Clés d\'identification'},
                project: {
                    tabs: {
                        actionsHeader: 'Actions',
                        api: {projectKeys: {table: {deleteKey: 'Supprimer la clé {{name}}'}}}
                    }
                }
            }
        });
        translate.use('fr');

        fixture = TestBed.createComponent(ProjectApiTabComponent);
        fixture.detectChanges();
    });

    it('should name the keys table with its level 2 heading (RGAA 5.2, 9.1)', () => {
        const heading: HTMLElement = fixture.nativeElement.querySelector('h2');
        const table: HTMLElement = fixture.nativeElement.querySelector('table');
        expect(heading.textContent.trim()).toBe('Clés d\'identification');
        expect(table.getAttribute('aria-labelledby')).toBe(heading.id);
        expect(table.hasAttribute('aria-describedby')).toBeFalse();
    });

    it('should give the delete button a name that includes the key name (RGAA 11.9)', () => {
        const button: HTMLButtonElement = fixture.nativeElement.querySelector('td button');
        expect(button.getAttribute('aria-label')).toBe('Supprimer la clé LBO_KEY');
        expect(button.type).toBe('button');
        expect(button.querySelector('mat-icon').getAttribute('aria-hidden')).toBe('true');
    });

    it('should not leave the action column header empty', () => {
        const headers = Array.from(fixture.nativeElement.querySelectorAll('th') as NodeListOf<HTMLElement>)
            .map(header => header.textContent.trim());
        expect(headers).toContain('Actions');
    });

    it('should open the documentation link in a new window and say so (RGAA 13.2)', () => {
        const link: HTMLAnchorElement = fixture.nativeElement.querySelector('.explanation-text a');
        expect(link.target).toBe('_blank');
        expect(link.rel).toBe('noopener noreferrer');
        expect(link.querySelector('.visually-hidden').textContent.trim()).toBe('(nouvelle fenêtre)');
        expect(link.title).toContain('(nouvelle fenêtre)');
        const icon: HTMLElement = link.querySelector('mat-icon');
        expect(icon.getAttribute('aria-hidden')).toBe('true');
        expect(icon.getAttribute('fontIcon')).toBe('open_in_new');
    });
});
