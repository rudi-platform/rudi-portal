import {ComponentFixture, TestBed} from '@angular/core/testing';

import {
    WorkflowFormSubmitSuccessComponent
} from '@shared/core/workflow/forms/workflow-form-submit-success/workflow-form-submit-success.component';

describe('WorkflowFormSubmitSuccessComponent', () => {
    let component: WorkflowFormSubmitSuccessComponent;
    let fixture: ComponentFixture<WorkflowFormSubmitSuccessComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [WorkflowFormSubmitSuccessComponent]
        })
            .compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(WorkflowFormSubmitSuccessComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
