import {NO_ERRORS_SCHEMA} from '@angular/core';
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {MatTableDataSource} from '@angular/material/table';
import {NgbPopover} from '@ng-bootstrap/ng-bootstrap';
import {provideTranslateService, TranslateService} from '@ngx-translate/core';
import {DatasetsInfosComponent} from '@shared/business/dataset/common/dataset-infos/dataset-infos.component';
import {DatasetsTableData, RowTableData} from '@shared/business/projects/projects-datasets-tables/dataset.interface';

import {DatasetTableComponent} from './dataset-table.component';

describe('DatasetTableComponent', () => {
    let fixture: ComponentFixture<DatasetTableComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [DatasetTableComponent],
            providers: [provideTranslateService()]
        })
            .overrideComponent(DatasetTableComponent, {
                remove: {imports: [DatasetsInfosComponent, NgbPopover]},
                add: {schemas: [NO_ERRORS_SCHEMA]}
            })
            .compileComponents();
        const translate = TestBed.inject(TranslateService);
        translate.setTranslation('fr', {
            personalSpace: {
                project: {
                    tabs: {
                        actionsHeader: 'Actions',
                        datasets: {
                            tableTitle: 'Jeux de données ouverts',
                            commentHeader: 'Commentaire',
                            removeItem: 'Retirer {{title}} de la réutilisation',
                            showComment: 'Afficher le commentaire sur {{title}}'
                        }
                    }
                }
            }
        });
        translate.use('fr');

        fixture = TestBed.createComponent(DatasetTableComponent);
        fixture.componentRef.setInput('title', 'personalSpace.project.tabs.datasets.tableTitle');
        fixture.componentRef.setInput('tableLoading', false);
        fixture.componentRef.setInput('hasDeleteButton', true);
        fixture.componentRef.setInput('hasCommentButton', true);
        fixture.componentRef.setInput('dataSource', new MatTableDataSource<RowTableData>([
            {datasetTitle: 'Arbres d\'alignement', addedDate: '05/11/2026'} as DatasetsTableData
        ]));
        fixture.detectChanges();
    });

    it('should render the table title as a level 2 heading (RGAA 9.1)', () => {
        const heading: HTMLElement = fixture.nativeElement.querySelector('h2.table-title-style');
        expect(heading?.textContent.trim()).toBe('Jeux de données ouverts');
    });

    it('should give the icon buttons a name that includes the dataset title (RGAA 11.9)', () => {
        const buttons = Array.from(fixture.nativeElement.querySelectorAll('td button') as NodeListOf<HTMLButtonElement>);
        expect(buttons.map(button => button.getAttribute('aria-label'))).toEqual([
            'Afficher le commentaire sur Arbres d\'alignement',
            'Retirer Arbres d\'alignement de la réutilisation'
        ]);
        buttons.forEach(button => {
            expect(button.type).toBe('button');
            expect(button.querySelector('mat-icon').getAttribute('aria-hidden')).toBe('true');
        });
    });

    it('should not leave the action column headers empty', () => {
        const headers = Array.from(fixture.nativeElement.querySelectorAll('th') as NodeListOf<HTMLElement>)
            .map(header => header.textContent.trim());
        expect(headers).not.toContain('');
        expect(headers).toContain('Commentaire');
        expect(headers).toContain('Actions');
    });
});
