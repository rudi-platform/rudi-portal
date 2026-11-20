import {ComponentFixture, TestBed} from '@angular/core/testing';
import {SelfdataDatasetService} from '@core/services/selfdata-dataset/selfdata-dataset.service';
import {TranslateService} from '@ngx-translate/core';
import {of} from 'rxjs';
import {SelfdataDataset} from './selfdata-dataset.interface';

import {SelfdataDatasetsTableComponent} from './selfdata-datasets-table.component';

describe('SelfdataDatasetsTableComponent', () => {
    let component: SelfdataDatasetsTableComponent;
    let fixture: ComponentFixture<SelfdataDatasetsTableComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [SelfdataDatasetsTableComponent],
            providers: [{
                provide: SelfdataDatasetService,
                useValue: jasmine.createSpyObj('SelfdataDatasetService', {searchSelfdataDatasets: of({total: 0, elements: []})})
            }]
        })
            .compileComponents();
        const translate = TestBed.inject(TranslateService);
        translate.setTranslation('fr', {
            common: {sortBy: 'Trier par {{column}}'},
            personalSpace: {selfdataDatasets: {date: 'Date'}}
        });
        translate.use('fr');
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(SelfdataDatasetsTableComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should expose each dataset through a link instead of a focusable row (RGAA 6.1, 7.3)', () => {
        component.total = 1;
        component.selfdataDatasetList = [{dataset_uuid: 'dataset-uuid', title: 'JDD LBO'} as SelfdataDataset];
        fixture.detectChanges();

        const row: HTMLElement = fixture.nativeElement.querySelector('tr.mat-mdc-row');
        const link: HTMLAnchorElement = row.querySelector('a.row-link');
        expect(link.textContent.trim()).toBe('JDD LBO');
        expect(link.getAttribute('href')).toBe('/selfdata-dataset-details/dataset-uuid');
        expect(row.hasAttribute('tabindex')).toBeFalse();
    });

    it('should name the table with a hidden caption (RGAA 5.4)', () => {
        component.total = 2;
        fixture.detectChanges();

        const caption: HTMLElement = fixture.nativeElement.querySelector('table caption');
        expect(caption.textContent.trim()).toContain('2');
        expect(caption.classList).toContain('visually-hidden');
    });

    it('should describe the sort action in French (RGAA 8.7)', () => {
        const sortButton: HTMLElement = fixture.nativeElement.querySelector('th.mat-column-updatedDate .mat-sort-header-container');
        const description = document.getElementById(sortButton.getAttribute('aria-describedby'));
        expect(description.textContent).toBe('Trier par Date');
    });
});
