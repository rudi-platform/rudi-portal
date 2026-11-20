import {ComponentFixture, TestBed} from '@angular/core/testing';
import {FormControl, FormGroup, Validators} from '@angular/forms';
import {DateAdapter, MAT_DATE_LOCALE} from '@angular/material/core';
import {MatDatepickerIntl, MatDatepickerModule} from '@angular/material/datepicker';
import {TranslatedMatDatepickerIntl} from '@core/i18n/translated-mat-datepicker-intl';
import moment from 'moment';
import {MonthYearDatepickerComponent} from './month-year-datepicker.component';

describe('MonthYearDatepickerComponent', () => {
    let fixture: ComponentFixture<MonthYearDatepickerComponent>;
    let formGroup: FormGroup;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [MonthYearDatepickerComponent, MatDatepickerModule],
            providers: [{provide: MAT_DATE_LOCALE, useValue: 'fr-FR'}]
        }).compileComponents();

        formGroup = new FormGroup({begin: new FormControl(null, Validators.required)});
        fixture = TestBed.createComponent(MonthYearDatepickerComponent);
        fixture.componentRef.setInput('formGroup', formGroup);
        fixture.componentRef.setInput('controlName', 'begin');
        fixture.componentRef.setInput('placeholder', 'Date de début');
        fixture.componentRef.setInput('errorMessage', 'Required');
        fixture.componentRef.setInput('ariaDescribedby', 'period-hint');
        fixture.detectChanges();
    });

    const input = (): HTMLInputElement => fixture.nativeElement.querySelector('#datepicker-begin');

    it('should link the expected format hint to the input (RGAA 11.10)', () => {
        expect(input().getAttribute('aria-describedby').split(' ')).toEqual(['period-hint']);
    });

    it('should link the error message to the input once displayed (RGAA 11.10)', () => {
        formGroup.get('begin').markAsDirty();
        formGroup.get('begin').updateValueAndValidity();
        fixture.detectChanges();

        const error: HTMLElement = fixture.nativeElement.querySelector('.error-label');
        expect(error.id).toBe('datepicker-begin-error');
        expect(error.querySelector('em').getAttribute('aria-hidden')).toBe('true');
        expect(input().getAttribute('aria-describedby').split(' ')).toEqual(['period-hint', 'datepicker-begin-error']);
    });

    it('should name the calendar button after its field (RGAA 11.9)', () => {
        const toggle: HTMLButtonElement = fixture.nativeElement.querySelector('mat-datepicker-toggle button');
        expect(toggle.getAttribute('aria-label')).toBe('datepicker.openCalendarFor');
    });

    it('should translate the calendar even under a module re-providing the English labels (RGAA 8.7)', () => {
        expect(fixture.debugElement.injector.get(MatDatepickerIntl)).toBeInstanceOf(TranslatedMatDatepickerIntl);
    });

    it('should display and vocalize the month names in French (RGAA 8.7)', () => {
        const adapter = fixture.debugElement.injector.get(DateAdapter);

        expect(adapter.getMonthNames('long')[7]).toBe('août');
        expect(adapter.format(moment('2026-09-01'), 'MMMM YYYY')).toBe('septembre 2026');
    });

    it('should not change the global moment locale used elsewhere in the application', () => {
        expect(moment.locale()).toBe('en');
    });
});
