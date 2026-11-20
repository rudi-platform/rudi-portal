import {TestBed} from '@angular/core/testing';
import {DisplayTableService} from '@core/services/data-set/display-table.service';
import {KonsultMetierService} from '@core/services/konsult-metier.service';
import {SPREADSHEET_COLDEF_INDEX} from '@features/data-set/components/spreadsheet/spreadsheet.component';
import {TranslateService} from '@ngx-translate/core';
import {GetBackendPropertyPipe} from '@shared/utils/pipes/get-backend-property.pipe';
import {KonsultService} from 'micro_service_modules/konsult/konsult-api';
import {utils, WorkBook} from 'xlsx';

describe('DisplayTableService', () => {
    let service: DisplayTableService;

    beforeEach(() => {
        TestBed.configureTestingModule({
            providers: [
                DisplayTableService,
                {provide: KonsultMetierService, useValue: {}},
                {provide: GetBackendPropertyPipe, useValue: {}},
                {provide: KonsultService, useValue: {}},
                {provide: TranslateService, useValue: {instant: (key: string): string => `translated:${key}`}}
            ]
        });
        service = TestBed.inject(DisplayTableService);
    });

    describe('convertToDisplayableData', () => {
        const workbook = {
            SheetNames: ['Sheet1'],
            Sheets: {Sheet1: utils.aoa_to_sheet([['name', 'city'], ['Air', 'Rennes']])}
        } as WorkBook;

        it('should give the row index column a translated header (RGAA 5.6)', () => {
            const result = service.convertToDisplayableData(workbook, true);

            expect(result.columnDefs[0].headerName).toBe('translated:metaData.tabulatedDataTab.rowNumberHeader');
            expect(result.columnDefs[0].headerClass).toBe('ag-index-header');
            expect(result.columnDefs.slice(1).map(colDef => colDef.field)).toEqual(['name', 'city']);
        });

        it('should not mutate the shared index column definition', () => {
            service.convertToDisplayableData(workbook, true);

            expect(SPREADSHEET_COLDEF_INDEX.headerName).toBeUndefined();
        });
    });
});
