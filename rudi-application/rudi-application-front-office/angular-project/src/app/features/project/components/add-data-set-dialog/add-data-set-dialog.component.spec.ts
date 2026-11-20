import {NO_ERRORS_SCHEMA} from '@angular/core';
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {MatDialogClose, MatDialogRef, MatDialogTitle} from '@angular/material/dialog';
import {TranslatePipe} from '@ngx-translate/core';
import {Subject} from 'rxjs';

import {AddDataSetDialogComponent} from './add-data-set-dialog.component';

describe('AjouterJeuDonneesDialogComponent', () => {
    let component: AddDataSetDialogComponent;
    let fixture: ComponentFixture<AddDataSetDialogComponent>;
    let keydownEvents: Subject<KeyboardEvent>;
    let dialogRef: jasmine.SpyObj<MatDialogRef<AddDataSetDialogComponent>>;

    beforeEach(async () => {
        keydownEvents = new Subject<KeyboardEvent>();
        dialogRef = jasmine.createSpyObj('MatDialogRef', {close: undefined, keydownEvents: keydownEvents.asObservable()});
        await TestBed.configureTestingModule({
            imports: [AddDataSetDialogComponent],
            providers: [{provide: MatDialogRef, useValue: dialogRef}]
        })
            .overrideComponent(AddDataSetDialogComponent, {
                set: {imports: [TranslatePipe, MatDialogTitle, MatDialogClose], schemas: [NO_ERRORS_SCHEMA]}
            })
            .compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(AddDataSetDialogComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
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

    it('should close without a dataset on Escape only (RGAA 7.1)', () => {
        component.selectMetadata({global_id: 'dataset'} as never);
        keydownEvents.next(new KeyboardEvent('keydown', {key: 'Escape', ctrlKey: true}));
        keydownEvents.next(new KeyboardEvent('keydown', {key: 'Enter'}));
        expect(dialogRef.close).not.toHaveBeenCalled();

        const escape = new KeyboardEvent('keydown', {key: 'Escape', cancelable: true});
        keydownEvents.next(escape);
        expect(dialogRef.close).toHaveBeenCalledOnceWith(null);
        expect(escape.defaultPrevented).toBeTrue();
    });
});
