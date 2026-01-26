import {ComponentFixture, TestBed} from '@angular/core/testing';

import {WorkflowFieldDateComponent} from '@shared/core/workflow/fields/workflow-field-date/workflow-field-date.component';

describe('WorkflowFieldTextComponent', () => {
    let component: WorkflowFieldDateComponent;
    let fixture: ComponentFixture<WorkflowFieldDateComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [WorkflowFieldDateComponent]
        })
            .compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(WorkflowFieldDateComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
