import {ComponentFixture, TestBed} from '@angular/core/testing';

import {WorkflowFormComponent} from '@shared/core/workflow/forms/workflow-form/workflow-form.component';

describe('WorkflowFormComponent', () => {
    let component: WorkflowFormComponent;
    let fixture: ComponentFixture<WorkflowFormComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [WorkflowFormComponent]
        })
            .compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(WorkflowFormComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
