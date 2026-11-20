import {ComponentFixture, TestBed} from '@angular/core/testing';

import {
    WorkflowFieldAttachmentPopinComponent
} from '@shared/core/workflow/fields/workflow-field-attachment-popin/workflow-field-attachment-popin.component';

describe('WorkflowFieldAttachmentPopinComponent', () => {
    let component: WorkflowFieldAttachmentPopinComponent;
    let fixture: ComponentFixture<WorkflowFieldAttachmentPopinComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [WorkflowFieldAttachmentPopinComponent]
        })
            .compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(WorkflowFieldAttachmentPopinComponent);
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

    it('should label the consent checkbox with its visible text (RGAA 11.1)', () => {
        const input: HTMLInputElement = fixture.nativeElement.querySelector('mat-checkbox input[type=checkbox]');
        const labelId = input.getAttribute('aria-labelledby');
        expect(labelId).toBe('attachment-consent-text');
        expect(fixture.nativeElement.querySelector(`#${labelId}`)).toBeTruthy();
    });
});
