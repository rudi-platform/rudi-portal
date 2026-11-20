import {ComponentFixture, TestBed} from '@angular/core/testing';
import {FormControl, FormGroup} from '@angular/forms';
import {provideTranslateService} from '@ngx-translate/core';

import {WorkflowFieldTextComponent} from '@shared/core/workflow/fields/workflow-field-text/workflow-field-text.component';
import {Field} from 'micro_service_modules/api-bpmn';

describe('WorkflowFieldTextComponent', () => {
    let component: WorkflowFieldTextComponent;
    let fixture: ComponentFixture<WorkflowFieldTextComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [WorkflowFieldTextComponent],
            providers: [provideTranslateService()]
        })
            .compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(WorkflowFieldTextComponent);
        component = fixture.componentInstance;
        fixture.componentRef.setInput('formGroup', new FormGroup({prefix_comment: new FormControl('')}));
        fixture.componentRef.setInput('formControlNamePrefix', 'prefix');
        fixture.componentRef.setInput('field', {definition: {name: 'comment', label: 'Commentaire'}} as Field);
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should not render the fallback name when a label is provided', () => {
        const label: HTMLLabelElement = fixture.nativeElement.querySelector('label');
        expect(label.querySelector('.visually-hidden')).toBeNull();
    });

    it('should render a hidden fallback name when the label is empty', () => {
        fixture.componentRef.setInput('field', {definition: {name: 'comment', label: ''}} as Field);
        fixture.detectChanges();

        const label: HTMLLabelElement = fixture.nativeElement.querySelector('label');
        const textarea: HTMLTextAreaElement = fixture.nativeElement.querySelector('textarea');
        expect(label.getAttribute('for')).toBe(textarea.id);
        expect(label.querySelector('.visually-hidden')?.textContent).toBe('common.comment');
    });

    it('should hide the decorative error icon', () => {
        fixture.componentRef.setInput('field', {definition: {name: 'comment', label: 'Commentaire', required: true}} as Field);
        fixture.detectChanges();
        component.formGroup.get('prefix_comment')?.setErrors({required: true});
        component.formGroup.get('prefix_comment')?.markAsTouched();
        fixture.detectChanges();

        const icon: HTMLElement = fixture.nativeElement.querySelector('mat-error em.fa');
        expect(icon.getAttribute('aria-hidden')).toBe('true');
    });
});
