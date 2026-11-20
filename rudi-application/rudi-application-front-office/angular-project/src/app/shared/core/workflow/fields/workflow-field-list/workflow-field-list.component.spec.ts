import {ComponentFixture, TestBed} from '@angular/core/testing';
import {FormControl, FormGroup} from '@angular/forms';

import {WorkflowFieldListComponent} from '@shared/core/workflow/fields/workflow-field-list/workflow-field-list.component';
import {Field} from 'micro_service_modules/api-bpmn';

describe('WorkflowFieldListComponent', () => {
    let component: WorkflowFieldListComponent;
    let fixture: ComponentFixture<WorkflowFieldListComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [WorkflowFieldListComponent]
        })
            .compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(WorkflowFieldListComponent);
        component = fixture.componentInstance;
        fixture.componentRef.setInput('formGroup', new FormGroup({prefix_mode: new FormControl(null)}));
        fixture.componentRef.setInput('formControlNamePrefix', 'prefix');
        fixture.componentRef.setInput('field', {
            definition: {
                name: 'mode',
                label: 'Mode',
                required: true,
                extendedType: '[{"code":"A","label":"Option A"}]'
            }
        } as Field);
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should name the select by its visible label, not by its value (RGAA 11.1)', () => {
        const select: HTMLElement = fixture.nativeElement.querySelector('mat-select');
        const labelId = select.getAttribute('aria-labelledby').trim();
        const label: HTMLElement = fixture.nativeElement.querySelector(`[id="${labelId}"]`);
        expect(label.tagName).toBe('LABEL');
        expect(label.textContent).toContain('Mode');
    });

    it('should convey the required state natively and hide the asterisk (RGAA 11.10)', () => {
        const asterisk: HTMLElement = fixture.nativeElement.querySelector('label .required-symbol');
        expect(asterisk.getAttribute('aria-hidden')).toBe('true');
        expect(asterisk.hasAttribute('aria-label')).toBeFalse();
        expect(fixture.nativeElement.querySelector('mat-select').getAttribute('aria-required')).toBe('true');
    });
});
