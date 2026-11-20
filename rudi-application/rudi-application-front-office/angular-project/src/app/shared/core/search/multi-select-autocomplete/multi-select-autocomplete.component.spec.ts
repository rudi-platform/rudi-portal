import {LiveAnnouncer} from '@angular/cdk/a11y';
import {DOWN_ARROW, ENTER} from '@angular/cdk/keycodes';
import {ComponentFixture, fakeAsync, flush, TestBed, tick} from '@angular/core/testing';
import {MatAutocompleteSelectedEvent} from '@angular/material/autocomplete';
import {SearchAutocompleteItem} from '@shared/core/search/search-autocomplete/search-autocomplete-item.interface';
import {MultiSelectAutocompleteComponent} from './multi-select-autocomplete.component';

describe('MultiSelectAutocompleteComponent', () => {
    let fixture: ComponentFixture<MultiSelectAutocompleteComponent<string>>;
    let component: MultiSelectAutocompleteComponent<string>;
    let announce: jasmine.Spy;

    const items: SearchAutocompleteItem<string>[] = [
        {label: 'Rennes Métropole', value: 'org-1'},
        {label: 'Ville de Rennes', value: 'org-2'}
    ] as SearchAutocompleteItem<string>[];

    // Material reads the legacy keyCode, which KeyboardEventInit does not set
    const keydown = (target: HTMLElement, keyCode: number, key: string): void => {
        const event = new KeyboardEvent('keydown', {key, bubbles: true, cancelable: true});
        Object.defineProperty(event, 'keyCode', {get: () => keyCode});
        target.dispatchEvent(event);
    };

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [MultiSelectAutocompleteComponent]
        }).compileComponents();

        announce = spyOn(TestBed.inject(LiveAnnouncer), 'announce').and.resolveTo();
        fixture = TestBed.createComponent(MultiSelectAutocompleteComponent<string>);
        component = fixture.componentInstance;
        fixture.componentRef.setInput('label', 'Organisations');
        fixture.componentRef.setInput('autocompleteItems', items);
        fixture.detectChanges();
    });

    afterEach(() => fixture.destroy());

    it('should select the active option with the Enter key (RGAA 7.3)', fakeAsync(() => {
        const selection = jasmine.createSpy('selectionChanged');
        component.selectionChanged.subscribe(selection);
        const input: HTMLInputElement = fixture.nativeElement.querySelector('input');
        input.focus();
        component.autocompleteTrigger.openPanel();
        fixture.detectChanges();

        keydown(input, DOWN_ARROW, 'ArrowDown');
        fixture.detectChanges();
        keydown(input, ENTER, 'Enter');
        fixture.detectChanges();
        flush();

        expect(selection).toHaveBeenCalledOnceWith(['org-1']);
        expect(announce).toHaveBeenCalledWith('multiSelect.itemAdded');
    }));

    it('should toggle an item from the selection event and expose its state to screen readers (RGAA 7.1)', fakeAsync(() => {
        const selectEvent = (item: SearchAutocompleteItem<string>): MatAutocompleteSelectedEvent =>
            ({option: jasmine.createSpyObj('MatOption', ['deselect'], {value: item})}) as unknown as MatAutocompleteSelectedEvent;
        const selectedHint = (): HTMLElement => fixture.nativeElement.ownerDocument.querySelector('mat-option .visually-hidden');
        // Clearing the input resets the list: the parent reloads it on searchTriggered
        component.searchTriggered.subscribe(() => fixture.componentRef.setInput('autocompleteItems', [...items]));

        const firstSelection = selectEvent(items[0]);
        component.onOptionSelected(firstSelection);
        tick(300);
        flush();
        fixture.detectChanges();

        expect(firstSelection.option.deselect).toHaveBeenCalledWith(false);
        expect(component.selectedItems).toEqual([items[0]]);
        expect(selectedHint()?.textContent).toContain('multiSelect.selectedOption');

        component.onOptionSelected(selectEvent(items[0]));
        flush();
        fixture.detectChanges();

        expect(component.selectedItems).toEqual([]);
        expect(selectedHint()).toBeNull();
        expect(announce).toHaveBeenCalledWith('multiSelect.itemRemoved');
    }));

    it('should let the keyboard reach and use the chip remove button (RGAA 7.3, 11.9)', fakeAsync(() => {
        component.selectedItems = [...items];
        fixture.detectChanges();
        const buttons: HTMLButtonElement[] = Array.from(fixture.nativeElement.querySelectorAll('mat-chip button'));

        expect(buttons.length).toBe(2);
        buttons.forEach(button => {
            expect(button.getAttribute('tabindex')).toBe('0');
            expect(button.getAttribute('type')).toBe('button');
            expect(button.getAttribute('aria-label')).toBe('multiSelect.removeItem');
            expect(button.querySelector('mat-icon').getAttribute('aria-hidden')).toBe('true');
        });

        buttons[0].focus();
        keydown(buttons[0], ENTER, 'Enter');
        fixture.detectChanges();
        flush();
        fixture.detectChanges();

        expect(component.selectedItems).toEqual([items[1]]);
        const remaining: HTMLButtonElement = fixture.nativeElement.querySelector('mat-chip button');
        expect(fixture.nativeElement.ownerDocument.activeElement).toBe(remaining);
    }));
});
