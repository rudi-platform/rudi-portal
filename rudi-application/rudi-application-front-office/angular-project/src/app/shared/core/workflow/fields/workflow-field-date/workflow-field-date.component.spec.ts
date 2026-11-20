import {ComponentFixture, TestBed} from '@angular/core/testing';
import {FormControl, FormGroup} from '@angular/forms';

import {WorkflowFieldDateComponent} from '@shared/core/workflow/fields/workflow-field-date/workflow-field-date.component';
import {Field} from 'micro_service_modules/api-bpmn';

describe('WorkflowFieldDateComponent', () => {
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
        fixture.componentRef.setInput('formGroup', new FormGroup({prefix_date: new FormControl('2024-01-31')}));
        fixture.componentRef.setInput('formControlNamePrefix', 'prefix');
        fixture.componentRef.setInput('field', {definition: {name: 'date', label: 'Date'}} as Field);
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
