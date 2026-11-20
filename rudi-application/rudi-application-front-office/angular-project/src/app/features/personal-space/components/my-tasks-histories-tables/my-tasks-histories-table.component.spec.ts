import {ComponentFixture, TestBed} from '@angular/core/testing';
import {TranslateService} from '@ngx-translate/core';

import {MyTasksHistoriesTableComponent} from './my-tasks-histories-table.component';

describe('MyTasksHistoriesTableComponent', () => {
    let fixture: ComponentFixture<MyTasksHistoriesTableComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [MyTasksHistoriesTableComponent]
        })
            .compileComponents();
        const translate = TestBed.inject(TranslateService);
        translate.setTranslation('fr', {
            common: {sortBy: 'Trier par {{column}}'},
            personalSpace: {
                myTasksHistories: {
                    date: 'Date',
                    project: {header: 'Réutilisation', singular: 'demande de publication de réutilisation'}
                }
            }
        });
        translate.use('fr');

        fixture = TestBed.createComponent(MyTasksHistoriesTableComponent);
        fixture.componentRef.setInput('backPaginatorId', 'projectTasksHistories');
        fixture.componentRef.setInput('icon', '');
        fixture.componentRef.setInput('tableTitleKey', 'personalSpace.myTasksHistories.project');
        fixture.componentRef.setInput('entries', [{id: 'historic-id', description: 'Réutilisation LBO', endTime: '2026-11-05T10:00:00Z'}]);
        fixture.detectChanges();
    });

    const sortDescription = (column: string): string => {
        const sortButton: HTMLElement = fixture.nativeElement.querySelector(`th.mat-column-${column} .mat-sort-header-container`);
        return document.getElementById(sortButton.getAttribute('aria-describedby')).textContent;
    };

    it('should describe the sort action in French (RGAA 8.7)', () => {
        expect(sortDescription('endDate')).toBe('Trier par Date');
        expect(sortDescription('description')).toBe('Trier par Réutilisation');
    });

    it('should hide the decorative header icon and name the table with a hidden caption (RGAA 1.2, 5.4)', () => {
        const icon: HTMLElement = fixture.nativeElement.querySelector('th.mat-column-description mat-icon');
        const caption: HTMLElement = fixture.nativeElement.querySelector('table caption');
        expect(icon.getAttribute('aria-hidden')).toBe('true');
        expect(caption.textContent.trim()).toBe('1 demande de publication de réutilisation');
        expect(caption.classList).toContain('visually-hidden');
    });
});
