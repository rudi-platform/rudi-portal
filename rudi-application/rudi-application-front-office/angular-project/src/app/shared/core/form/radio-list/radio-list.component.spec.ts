import {ComponentFixture, TestBed} from '@angular/core/testing';
import {FormControl, FormGroup, Validators} from '@angular/forms';

import {RadioListItem} from '@shared/core/form/radio-list/radio-list-item';
import {RadioListComponent} from '@shared/core/form/radio-list/radio-list.component';

describe('RadioListComponent', () => {
    let fixture: ComponentFixture<RadioListComponent>;

    const suggestions: RadioListItem[] = [
        {code: 'OPEN', label: 'Open', description: 'Open project'},
        {code: 'PRIVATE', label: 'Private', description: 'Private project'}
    ];

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [RadioListComponent]
        })
            .compileComponents();
    });

    function render(control: FormControl): HTMLInputElement[] {
        fixture = TestBed.createComponent(RadioListComponent);
        fixture.componentRef.setInput('formGroup', new FormGroup({confidentiality: control}));
        fixture.componentRef.setInput('controlName', 'confidentiality');
        fixture.componentRef.setInput('suggestions', suggestions);
        fixture.componentRef.setInput('isConfidentialityValid', true);
        fixture.detectChanges();
        return Array.from(fixture.nativeElement.querySelectorAll('input[type="radio"]'));
    }

    it('should mark every radio as required when the control is required (RGAA 11.10)', () => {
        const radios = render(new FormControl('', Validators.required));
        expect(fixture.componentInstance.isRequired).toBeTrue();
        expect(radios.length).toBe(suggestions.length);
        radios.forEach(radio => expect(radio.required).toBeTrue());
    });

    it('should not mark the radios as required when the control is optional', () => {
        const radios = render(new FormControl(''));
        expect(fixture.componentInstance.isRequired).toBeFalse();
        radios.forEach(radio => expect(radio.required).toBeFalse());
    });

    it('should not name the radio group by default', () => {
        render(new FormControl(''));
        const group: HTMLElement = fixture.nativeElement.querySelector('mat-radio-group');
        expect(group.hasAttribute('aria-labelledby')).toBeFalse();
        expect(group.hasAttribute('aria-describedby')).toBeFalse();
    });

    it('should name and describe the radio group (RGAA 11.5, 11.6)', () => {
        render(new FormControl(''));
        fixture.componentRef.setInput('ariaLabelledby', 'group-label');
        fixture.componentRef.setInput('ariaDescribedby', 'group-hint');
        fixture.detectChanges();
        const group: HTMLElement = fixture.nativeElement.querySelector('mat-radio-group');
        expect(group.getAttribute('aria-labelledby')).toBe('group-label');
        expect(group.getAttribute('aria-describedby')).toBe('group-hint');
    });

    it('should describe each radio by its option detail (RGAA 11.2)', () => {
        const radios = render(new FormControl(''));
        radios.forEach((radio, i) => {
            const detail = fixture.nativeElement.querySelector(`#${radio.getAttribute('aria-describedby')}`);
            expect(detail?.textContent.trim()).toBe(suggestions[i].description);
        });
    });
});
