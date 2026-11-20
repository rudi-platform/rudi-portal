import {ComponentFixture, TestBed} from '@angular/core/testing';

import {WorkflowFormDialogComponent} from '@shared/core/workflow/forms/workflow-form-dialog/workflow-form-dialog.component';

describe('WorkflowFormDialogComponent', () => {
    let component: WorkflowFormDialogComponent;
    let fixture: ComponentFixture<WorkflowFormDialogComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [WorkflowFormDialogComponent]
        })
            .compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(WorkflowFormDialogComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should focus the first invalid field when confirm fails', () => {
        component.dialogData = {...component.dialogData, form: {} as typeof component.dialogData.form};
        spyOn(component.workflowFormComponent, 'submit').and.returnValue(false);
        const invalidField = document.createElement('textarea');
        invalidField.classList.add('ng-invalid');
        fixture.nativeElement.appendChild(invalidField);

        component.onClickConfirm();

        expect(document.activeElement).toBe(invalidField);
    });

    it('should not move focus when confirm succeeds', () => {
        component.dialogData = {...component.dialogData, form: {} as typeof component.dialogData.form};
        spyOn(component.workflowFormComponent, 'submit').and.returnValue(true);
        const invalidField = document.createElement('textarea');
        invalidField.classList.add('ng-invalid');
        fixture.nativeElement.appendChild(invalidField);

        component.onClickConfirm();

        expect(document.activeElement).not.toBe(invalidField);
    });
});
