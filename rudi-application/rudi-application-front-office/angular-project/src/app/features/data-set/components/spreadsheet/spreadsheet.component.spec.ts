import {ComponentFixture, TestBed} from '@angular/core/testing';

import {SpreadsheetComponent} from './spreadsheet.component';

describe('SpreadsheetComponent', () => {
    let component: SpreadsheetComponent;
    let fixture: ComponentFixture<SpreadsheetComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [SpreadsheetComponent]
        })
            .compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(SpreadsheetComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should let the browser handle Tab so the grid is a single tab stop', () => {
        expect(component.gridOptions.tabToNextHeader?.({} as never)).toBeFalse();
        expect(component.gridOptions.tabToNextCell?.({} as never)).toBeFalse();
    });

    it('should keep rows and cells in DOM order so screen readers announce the right positions', () => {
        expect(component.gridOptions.ensureDomOrder).toBeTrue();
    });

    it('should remove grid scroll viewports from the tab order', () => {
        const host: HTMLElement = fixture.nativeElement;
        const viewport = document.createElement('div');
        viewport.className = 'ag-body-vertical-scroll-viewport';
        host.appendChild(viewport);

        component['removeScrollViewportsFromTabOrder']();

        expect(viewport.getAttribute('tabindex')).toBe('-1');
    });

    it('should move focus to the column header when Shift+Tab from a cell lands on the cells scroll container', () => {
        const host: HTMLElement = fixture.nativeElement;
        const header = document.createElement('div');
        header.setAttribute('role', 'columnheader');
        header.tabIndex = -1;
        const viewport = document.createElement('div');
        viewport.className = 'ag-center-cols-viewport';
        const cell = document.createElement('div');
        cell.setAttribute('role', 'gridcell');
        viewport.appendChild(cell);
        host.append(header, viewport);
        const focusSpy = spyOn(header, 'focus');

        component.handleFocusIn({target: viewport, relatedTarget: cell} as unknown as FocusEvent);
        expect(focusSpy).toHaveBeenCalled();

        focusSpy.calls.reset();
        component.handleFocusIn({target: viewport, relatedTarget: null} as unknown as FocusEvent);
        expect(focusSpy).not.toHaveBeenCalled();
    });
});
