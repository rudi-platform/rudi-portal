import {ComponentFixture, TestBed} from '@angular/core/testing';
import {FormControl, FormGroup} from '@angular/forms';

import {WorkflowFieldTemplateComponent} from '@shared/core/workflow/fields/workflow-field-template/workflow-field-template.component';
import {Field, Section} from 'micro_service_modules/api-bpmn';

describe('WorkflowFieldTemplateComponent', () => {
    let component: WorkflowFieldTemplateComponent;
    let fixture: ComponentFixture<WorkflowFieldTemplateComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [WorkflowFieldTemplateComponent]
        })
            .compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(WorkflowFieldTemplateComponent);
        component = fixture.componentInstance;
        fixture.componentRef.setInput('formGroup', new FormGroup({section_comment: new FormControl('')}));
        fixture.componentRef.setInput('section', {name: 'section'} as Section);
        fixture.componentRef.setInput('field', {definition: {name: 'comment', type: 'STRING'}} as Field);
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
