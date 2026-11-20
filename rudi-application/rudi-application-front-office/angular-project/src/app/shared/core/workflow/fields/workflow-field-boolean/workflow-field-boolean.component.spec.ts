import {ComponentFixture, TestBed} from '@angular/core/testing';
import {FormControl, FormGroup} from '@angular/forms';
import {provideTranslateService} from '@ngx-translate/core';

import {WorkflowFieldBooleanComponent} from '@shared/core/workflow/fields/workflow-field-boolean/workflow-field-boolean.component';
import {Field} from 'micro_service_modules/api-bpmn';

describe('WorkflowFieldBooleanComponent', () => {
    let component: WorkflowFieldBooleanComponent;
    let fixture: ComponentFixture<WorkflowFieldBooleanComponent>;
    let formGroup: FormGroup;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [WorkflowFieldBooleanComponent],
            providers: [provideTranslateService()]
        })
            .compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(WorkflowFieldBooleanComponent);
        component = fixture.componentInstance;
        formGroup = new FormGroup({prefix_agree: new FormControl(false)});
        fixture.componentRef.setInput('formGroup', formGroup);
        fixture.componentRef.setInput('formControlNamePrefix', 'prefix');
        fixture.componentRef.setInput('field', {
            definition: {
                name: 'agree',
                label: 'I agree',
                required: true
            }
        } as Field);
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should name the native checkbox with the visible label (RGAA 11.1)', () => {
        const input: HTMLInputElement = fixture.nativeElement.querySelector('input[type="checkbox"]');
        const label: HTMLLabelElement = fixture.nativeElement.querySelector('.label-area label');
        expect(label.htmlFor).toBe(input.id);
        expect(input.labels[input.labels.length - 1]).toBe(label);
    });

    it('should toggle the checkbox when the visible label is clicked', () => {
        const label: HTMLLabelElement = fixture.nativeElement.querySelector('.label-area label');
        label.click();
        fixture.detectChanges();
        expect(formGroup.get('prefix_agree').value).toBeTrue();
    });

    it('should hide the required asterisk from assistive technologies (RGAA 11.10)', () => {
        const asterisk: HTMLElement = fixture.nativeElement.querySelector('label .required-symbol');
        expect(asterisk.getAttribute('aria-hidden')).toBe('true');
        expect(asterisk.hasAttribute('aria-label')).toBeFalse();
    });
});
