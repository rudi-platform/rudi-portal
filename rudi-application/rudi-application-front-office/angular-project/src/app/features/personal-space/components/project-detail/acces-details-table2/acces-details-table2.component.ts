import {AfterViewInit, Component, Input, ViewChild} from '@angular/core';
import {MatIcon, MatIconRegistry} from '@angular/material/icon';
import {MatSort, MatSortHeader} from '@angular/material/sort';
import {
    MatCell,
    MatCellDef,
    MatColumnDef,
    MatHeaderCell,
    MatHeaderCellDef,
    MatHeaderRow,
    MatHeaderRowDef,
    MatRow,
    MatRowDef,
    MatTable,
    MatTableDataSource
} from '@angular/material/table';
import {DomSanitizer} from '@angular/platform-browser';
import {TranslateDirective, TranslatePipe} from '@ngx-translate/core';
import {NewDatasetRequest} from 'micro_service_modules/projekt/projekt-api';
import moment from 'moment';

export interface Table2Data {
    date: string;
    titre: string;
    statut: string;
}

@Component({
    selector: 'app-acces-details-table2',
    templateUrl: './acces-details-table2.component.html',
    styleUrls: ['./acces-details-table2.component.scss'],
    imports: [TranslateDirective, MatTable, MatSort, MatColumnDef, MatHeaderCellDef, MatHeaderCell, MatSortHeader, MatCellDef, MatCell, MatIcon, MatHeaderRowDef, MatHeaderRow, MatRowDef, MatRow, TranslatePipe]
})
export class AccesDetailsTable2Component implements AfterViewInit {
    jdds: Table2Data[] = [];
    displayedColumns: string[] = ['date', 'titre', 'statut'];
    dataSource: MatTableDataSource<Table2Data> = new MatTableDataSource(this.jdds);

    @Input()
    set newDatasetRequests(value: NewDatasetRequest[]) {
        if (value && value.length > 0) {
            this.jdds = value.map((request: NewDatasetRequest) => {
                return {
                    titre: request.title,
                    date: moment(request.updated_date).format('DD/MM/YYYY'),
                    statut: request.functional_status,
                };
            });

            this.dataSource = new MatTableDataSource(this.jdds);
        }
    }

    constructor(
        private readonly domSanitizer: DomSanitizer,
        private readonly matIconRegistry: MatIconRegistry,
    ) {
        this.matIconRegistry.addSvgIcon(
            'nouvelles_donnees',
            this.domSanitizer.bypassSecurityTrustResourceUrl('../assets/pictos/nouvelles_donnees.svg')
        );
    }

    @ViewChild(MatSort) sort: MatSort;

    ngAfterViewInit() {
        this.dataSource.sort = this.sort;
    }
}
