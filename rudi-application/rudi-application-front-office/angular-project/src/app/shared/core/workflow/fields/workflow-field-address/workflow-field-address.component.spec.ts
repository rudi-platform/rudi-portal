import {ComponentFixture, TestBed} from '@angular/core/testing';
import {FormControl, FormGroup} from '@angular/forms';

import {WorkflowFieldAddressComponent} from '@shared/core/workflow/fields/workflow-field-address/workflow-field-address.component';
import {Field} from 'micro_service_modules/api-bpmn';

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
        fixture.componentRef.setInput('formGroup', new FormGroup({prefix_address: new FormControl(null)}));
        fixture.componentRef.setInput('formControlNamePrefix', 'prefix');
        fixture.componentRef.setInput('field', {definition: {name: 'address', label: 'Adresse'}} as Field);
        // Appelé par WorkflowFieldTemplateComponent avant le premier rendu
        component.addOtherControls();
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
