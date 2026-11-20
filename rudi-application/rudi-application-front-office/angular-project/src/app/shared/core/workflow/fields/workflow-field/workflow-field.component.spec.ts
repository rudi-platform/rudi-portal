import {ComponentFixture, TestBed} from '@angular/core/testing';
import {FormControl, FormGroup} from '@angular/forms';

import {WorkflowFieldComponent} from '@shared/core/workflow/fields/workflow-field/workflow-field.component';
import {Field} from 'micro_service_modules/api-bpmn';

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
        fixture.componentRef.setInput('formGroup', new FormGroup({prefix_comment: new FormControl('')}));
        fixture.componentRef.setInput('formControlNamePrefix', 'prefix');
        fixture.componentRef.setInput('field', {definition: {name: 'comment', label: 'Commentaire'}} as Field);
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should convey the required state natively and hide the asterisk (RGAA 11.10)', () => {
        fixture.componentRef.setInput('field', {definition: {name: 'comment', label: 'Commentaire', required: true}} as Field);
        fixture.detectChanges();

        const asterisk: HTMLElement = fixture.nativeElement.querySelector('label .required-symbol');
        expect(asterisk.getAttribute('aria-hidden')).toBe('true');
        expect(asterisk.hasAttribute('aria-label')).toBeFalse();
        expect(fixture.nativeElement.querySelector('#input-prefix_comment').getAttribute('aria-required')).toBe('true');
    });
});
