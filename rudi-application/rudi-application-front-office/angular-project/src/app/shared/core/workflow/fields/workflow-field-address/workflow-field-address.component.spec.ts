import {ComponentFixture, TestBed} from '@angular/core/testing';

import {WorkflowFieldAddressComponent} from '@shared/core/workflow/fields/workflow-field-address/workflow-field-address.component';

describe('WorkflowFieldAddressComponent', () => {
    let component: WorkflowFieldAddressComponent;
    let fixture: ComponentFixture<WorkflowFieldAddressComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [WorkflowFieldAddressComponent]
        })
            .compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(WorkflowFieldAddressComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
