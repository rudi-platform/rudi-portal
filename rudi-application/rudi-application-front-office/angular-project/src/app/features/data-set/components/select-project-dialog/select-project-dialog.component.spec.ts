import {ComponentFixture, TestBed} from '@angular/core/testing';
import {MAT_DIALOG_DATA} from '@angular/material/dialog';

import {SelectProjectDialogComponent} from './select-project-dialog.component';

describe('SelectProjectDialogComponent', () => {
    let fixture: ComponentFixture<SelectProjectDialogComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [SelectProjectDialogComponent],
            providers: [{provide: MAT_DIALOG_DATA, useValue: {data: {metadata: {global_id: 'dataset-uuid'}}}}]
        })
            .compileComponents();

        fixture = TestBed.createComponent(SelectProjectDialogComponent);
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
});
