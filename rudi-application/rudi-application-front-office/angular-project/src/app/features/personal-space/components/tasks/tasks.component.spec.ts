import {ComponentFixture, TestBed} from '@angular/core/testing';
import {RequestToStudy} from '@core/services/tasks-aggregator/request-to-study.interface';
import {TranslateService} from '@ngx-translate/core';

import {TasksComponent} from './tasks.component';

describe('RequestsToStudyTabComponent', () => {
    let component: TasksComponent;
    let fixture: ComponentFixture<TasksComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [TasksComponent]
        })
            .compileComponents();
        const translate = TestBed.inject(TranslateService);
        translate.setTranslation('fr', {
            common: {sortBy: 'Trier par {{column}}'},
            personalSpace: {myNotifications: {title: 'Mes notifications', receivedDate: 'Date de réception'}}
        });
        translate.use('fr');
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(TasksComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should expose each request through a link instead of a focusable row (RGAA 6.1, 7.3)', () => {
        fixture.componentRef.setInput('requestsToStudy', [
            {taskId: 'task-id', url: 'project-task-detail', description: 'Demande LBO', receivedDate: new Date(2026, 10, 5)} as RequestToStudy
        ]);
        fixture.detectChanges();

        const row: HTMLElement = fixture.nativeElement.querySelector('tr.mat-mdc-row');
        const link: HTMLAnchorElement = row.querySelector('a.row-link');
        expect(link.textContent.trim()).toBe('Demande LBO');
        expect(link.getAttribute('href')).toBe('/project-task-detail/task-id');
        expect(row.hasAttribute('tabindex')).toBeFalse();
    });

    it('should name the table with a hidden caption instead of a description (RGAA 5.4)', () => {
        const table: HTMLElement = fixture.nativeElement.querySelector('table');
        const caption: HTMLElement = table.querySelector('caption');
        expect(caption.textContent.trim()).toBe('Mes notifications');
        expect(caption.classList).toContain('visually-hidden');
        expect(table.hasAttribute('aria-describedby')).toBeFalse();
    });

    it('should expose the count as a level 2 heading (RGAA 9.1)', () => {
        const count: HTMLElement = fixture.nativeElement.querySelector('app-search-count');
        expect(count.getAttribute('role')).toBe('heading');
        expect(count.getAttribute('aria-level')).toBe('2');
    });

    it('should describe the sort action in French (RGAA 8.7)', () => {
        const sortButton: HTMLElement = fixture.nativeElement.querySelector('th.mat-column-receivedDate .mat-sort-header-container');
        const description = document.getElementById(sortButton.getAttribute('aria-describedby'));
        expect(description.textContent).toBe('Trier par Date de réception');
    });
});
