import {ComponentFixture, TestBed} from '@angular/core/testing';

import {WorkflowFieldComponent} from '@shared/core/workflow/fields/workflow-field/workflow-field.component';

describe('WorkflowFieldComponent', () => {
    let component: WorkflowFieldComponent;
    let fixture: ComponentFixture<WorkflowFieldComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [WorkflowFieldComponent]
        })
            .compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(WorkflowFieldComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
