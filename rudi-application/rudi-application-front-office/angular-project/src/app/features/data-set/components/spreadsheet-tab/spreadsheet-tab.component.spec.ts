import {ComponentFixture, TestBed} from '@angular/core/testing';
import {SpreadsheetTabComponent} from '@features/data-set/components/spreadsheet-tab/spreadsheet-tab.component';

describe('SpreadsheetTabComponent', () => {
    let component: SpreadsheetTabComponent;
    let fixture: ComponentFixture<SpreadsheetTabComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [SpreadsheetTabComponent]
        })
            .compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(SpreadsheetTabComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    describe('results count (RGAA 7.5)', () => {
        const liveRegion = (): HTMLElement => fixture.nativeElement.querySelector('output[aria-live="polite"]');

        it('should keep an empty polite live region in the DOM before any search', () => {
            expect(liveRegion()).not.toBeNull();
            expect(liveRegion().textContent?.trim()).toBe('');
        });

        it('should render the results count inside the live region after a search', () => {
            component.displayTableData = {columnDefs: [], rowData: [{a: 'x'}, {a: 'y'}]} as never;
            component.displayResults = true;
            fixture.detectChanges();

            expect(liveRegion().textContent).toContain('2');
        });
    });
});
