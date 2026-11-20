import {ComponentFixture, TestBed} from '@angular/core/testing';
import {MAT_DIALOG_DATA} from '@angular/material/dialog';

import {RequestDetailsDialogComponent} from './request-details-dialog.component';

describe('RequestDetailsDialogComponent', () => {
    let component: RequestDetailsDialogComponent;
    let fixture: ComponentFixture<RequestDetailsDialogComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [RequestDetailsDialogComponent],
            providers: [{provide: MAT_DIALOG_DATA, useValue: {data: {endDate: null, requestDetails: null}}}]
        })
            .compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(RequestDetailsDialogComponent);
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

    it('should name the end date field by its visible label and give the expected format (RGAA 11.1 / 11.10)', () => {
        const input: HTMLInputElement = fixture.nativeElement.querySelector('#datepicker-date');
        const label: HTMLElement = fixture.nativeElement.querySelector(`#${input.getAttribute('aria-labelledby')}`);
        const hint: HTMLElement = fixture.nativeElement.querySelector(`#${input.getAttribute('aria-describedby')}`);

        expect(label.textContent).toContain('project.request-details-dialog.date');
        expect(hint.textContent).toContain('project.request-details-dialog.dateFormatHint');
    });

    it('should name the comment field by its visible label and link its hint (RGAA 11.1 / 11.10)', () => {
        const textarea: HTMLTextAreaElement = fixture.nativeElement.querySelector('textarea');
        const label: HTMLElement = fixture.nativeElement.querySelector(`#${textarea.getAttribute('aria-labelledby')}`);

        expect(label.textContent).toContain('project.request-details-dialog.comment');
        expect(textarea.getAttribute('aria-describedby').split(' ')).toContain('request-details-comment-hint');
    });
});
