import {ComponentFixture, TestBed} from '@angular/core/testing';

import {SpreadsheetSearchComponent} from '@features/data-set/components/spreadsheet-search/spreadsheet-search.component';

describe('SpreadsheetSearchComponent', () => {
    let component: SpreadsheetSearchComponent;
    let fixture: ComponentFixture<SpreadsheetSearchComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [SpreadsheetSearchComponent]
        })
            .compileComponents();

        fixture = TestBed.createComponent(SpreadsheetSearchComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should not show the clear button while the field is empty', () => {
        expect(fixture.nativeElement.querySelector('button')).toBeNull();
    });

    it('should clear through a named native button and give focus back to the field (RGAA 7.3)', () => {
        const emitSpy = spyOn(component.searchTermsEmitter, 'emit');
        component.searchTerms = 'abc';
        fixture.detectChanges();

        const button: HTMLButtonElement = fixture.nativeElement.querySelector('button');
        expect(button.type).toBe('button');
        expect(button.getAttribute('aria-label')).toBe('common.clearSearch');
        expect(button.querySelector('mat-icon').getAttribute('aria-hidden')).toBe('true');

        button.click();
        fixture.detectChanges();

        expect(component.searchTerms).toBe('');
        expect(emitSpy).toHaveBeenCalledWith('');
        expect(document.activeElement).toBe(fixture.nativeElement.querySelector('input'));
    });
});
