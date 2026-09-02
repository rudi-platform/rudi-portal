import {Component, ElementRef, Input, Renderer2, ViewChild} from '@angular/core';
import {BreakpointObserverService} from '@core/services/breakpoint-observer.service';
import {AgGridAngular} from 'ag-grid-angular';
import {ColDef, GridOptions} from 'ag-grid-community';
import {SPREADSHEET_LOCALE_FR} from './spreadsheet-locale-fr';

export const SPREADSHEET_COLDEF_INDEX: ColDef = {
    field: '',
    width: 75,
    valueGetter: 'node.rowIndex + 1',
    cellClass: 'ag-first-cell-column'
};

@Component({
    selector: 'app-spreadsheet',
    templateUrl: './spreadsheet.component.html',
    styleUrls: ['./spreadsheet.component.scss'],
    imports: [AgGridAngular]
})
export class SpreadsheetComponent {

    private static nextId = 0;

    readonly headingId = `spreadsheet-caption-${SpreadsheetComponent.nextId++}`;

    constructor(
        private readonly breakpointObserver: BreakpointObserverService,
        private readonly elementRef: ElementRef<HTMLElement>,
        private readonly renderer: Renderer2,
    ) {
        this.defaultColDef = SpreadsheetComponent.createDefaultColDef();
    }

    private static readonly MAX_COL_SM_SCREEN = 2;
    private static readonly MAX_COL_MD_SCREEN = 3;
    private static readonly MAX_COL_LG_SCREEN = 6;
    private static readonly MAX_COL_XL_SCREEN = 8;
    private static readonly MAX_COL_XXL_SCREEN = 10;

    @ViewChild(AgGridAngular) grid?: AgGridAngular;

    gridOptions: GridOptions = {
        localeText: SPREADSHEET_LOCALE_FR
    };

    @Input()
    caption = '';

    @Input()
    public rowData: unknown[] = [];

    @Input()
    columnDefs: ColDef[];

    public defaultColDef: ColDef;

    /**
     * Méthode qui initialise le tri
     * @private
     */
    private static createDefaultColDef(): ColDef {
        return {
            sortable: true,
            resizable: true,
        };
    }

    fitColumnSize(): void {
        if (this.columnDefs.length <= this.mediaSizeGestion()) {
            this.grid?.api.sizeColumnsToFit();
        }
        this.labelGridForAccessibility();
    }

    /**
     * AG Grid génère lui-même, au sein du composant <ag-grid-angular>, l'élément portant le rôle
     * ARIA "grid"/"treegrid". C'est cet élément interne (et non le composant <ag-grid-angular>,
     * qui ne porte aucun rôle ARIA) qui doit recevoir l'attribut aria-labelledby pour que le nom
     * accessible du tableau soit correctement restitué par les lecteurs d'écran.
     */
    private labelGridForAccessibility(): void {
        const gridRoleElement = this.elementRef.nativeElement.querySelector('[role="grid"], [role="treegrid"]');
        if (!gridRoleElement) {
            return;
        }
        if (this.caption) {
            this.renderer.setAttribute(gridRoleElement, 'aria-labelledby', this.headingId);
        } else {
            this.renderer.removeAttribute(gridRoleElement, 'aria-labelledby');
        }
    }

    private mediaSizeGestion(): number {
        const mediaSize = this.breakpointObserver.getMediaSize();
        if (mediaSize.isSm) {
            return SpreadsheetComponent.MAX_COL_SM_SCREEN;
        }
        if (mediaSize.isMd) {
            return SpreadsheetComponent.MAX_COL_MD_SCREEN;
        }
        if (mediaSize.isLg) {
            return SpreadsheetComponent.MAX_COL_LG_SCREEN;
        }
        if (mediaSize.isXl) {
            return SpreadsheetComponent.MAX_COL_XL_SCREEN;
        }

        return SpreadsheetComponent.MAX_COL_XXL_SCREEN;
    }


}
