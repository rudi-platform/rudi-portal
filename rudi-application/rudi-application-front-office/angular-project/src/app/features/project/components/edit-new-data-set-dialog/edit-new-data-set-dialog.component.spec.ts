import {ComponentFixture, TestBed} from '@angular/core/testing';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {Subject} from 'rxjs';

import {EditNewDataSetDialogComponent} from './edit-new-data-set-dialog.component';

describe('EditNewDataSetDialogComponent', () => {
    let fixture: ComponentFixture<EditNewDataSetDialogComponent>;
    let keydownEvents: Subject<KeyboardEvent>;
    let dialogRef: jasmine.SpyObj<MatDialogRef<EditNewDataSetDialogComponent>>;

    beforeEach(async () => {
        keydownEvents = new Subject<KeyboardEvent>();
        dialogRef = jasmine.createSpyObj('MatDialogRef', {close: undefined, keydownEvents: keydownEvents.asObservable()});
        await TestBed.configureTestingModule({
            imports: [EditNewDataSetDialogComponent],
            providers: [
                {provide: MAT_DIALOG_DATA, useValue: {data: {dataRequestItem: null, counter: 1}}},
                {provide: MatDialogRef, useValue: dialogRef}
            ]
        })
            .compileComponents();

        fixture = TestBed.createComponent(EditNewDataSetDialogComponent);
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(fixture.componentInstance).toBeTruthy();
    });

    it('should give the close button an accessible name (RGAA 11.9)', () => {
        const button: HTMLButtonElement = fixture.nativeElement.querySelector('.dialog-cross-button button');
        expect(button.getAttribute('type')).toBe('button');
        expect(button.getAttribute('aria-label')).toBe('popup.close');
        expect(button.querySelector('mat-icon').getAttribute('aria-hidden')).toBe('true');
    });

    it('should name the dialog by its visible title (RGAA 7.1)', () => {
        const title: HTMLElement = fixture.nativeElement.querySelector('.dialog-title');
        expect(title.classList).toContain('mat-mdc-dialog-title');
        expect(title.id).toMatch(/^mat-mdc-dialog-title-/);
    });

    it('should label each field and link its hint (RGAA 11.1, 11.2)', () => {
        ['new-dataset-request-title', 'new-dataset-request-description'].forEach(id => {
            const field: HTMLElement = fixture.nativeElement.querySelector(`#${id}`);
            const label: HTMLElement = fixture.nativeElement.querySelector(`#${field.getAttribute('aria-labelledby')}`);
            expect(label?.tagName).withContext(id).toBe('MAT-LABEL');
            expect(label?.textContent).withContext(id).toContain('project.dialogEditNewDatasetRequest.');
            expect(field.getAttribute('aria-describedby').split(' ')).withContext(id).toContain(`${id}-hint`);
            expect(field.getAttribute('aria-required')).withContext(id).toBe('true');
        });
    });

    it('should close without a request on Escape only (RGAA 7.1)', () => {
        keydownEvents.next(new KeyboardEvent('keydown', {key: 'Escape', altKey: true}));
        keydownEvents.next(new KeyboardEvent('keydown', {key: 'Enter'}));
        expect(dialogRef.close).not.toHaveBeenCalled();

        const escape = new KeyboardEvent('keydown', {key: 'Escape', cancelable: true});
        keydownEvents.next(escape);
        expect(dialogRef.close).toHaveBeenCalledOnceWith(null);
        expect(escape.defaultPrevented).toBeTrue();
    });
});
