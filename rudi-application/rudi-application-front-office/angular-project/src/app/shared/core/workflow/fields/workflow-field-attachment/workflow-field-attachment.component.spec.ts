import {ComponentFixture, TestBed} from '@angular/core/testing';
import {FormControl, FormGroup} from '@angular/forms';
import {FilePickerModule} from '@sleiss/ngx-awesome-uploader';

import {WorkflowFieldAttachmentComponent} from '@shared/core/workflow/fields/workflow-field-attachment/workflow-field-attachment.component';
import {WorkflowProperties} from '@shared/core/workflow/forms/workflow-form/workflow-properties';
import {DataSize} from '@shared/models/data-size';
import {Field} from 'micro_service_modules/api-bpmn';

describe('WorkflowFieldAttachementComponent', () => {
    let component: WorkflowFieldAttachmentComponent;
    let fixture: ComponentFixture<WorkflowFieldAttachmentComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [WorkflowFieldAttachmentComponent, FilePickerModule]
        })
            .compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(WorkflowFieldAttachmentComponent);
        component = fixture.componentInstance;
        fixture.componentRef.setInput('formGroup', new FormGroup({prefix_attachment: new FormControl(null)}));
        fixture.componentRef.setInput('formControlNamePrefix', 'prefix');
        fixture.componentRef.setInput('field', {definition: {name: 'attachment', label: 'Pièce jointe'}} as Field);
        fixture.componentRef.setInput('properties', {fileMaxSize: DataSize.ofMegabytes(10)} as WorkflowProperties);
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should download the read-only attachment with a named native button (RGAA 7.3, 11.9)', () => {
        fixture.componentRef.setInput('field', {definition: {name: 'attachment', label: 'Pièce jointe', readOnly: true}} as Field);
        component.attachment = {name: 'doc.pdf', size: 12} as typeof component.attachment;
        component.attachmentLoading = false;
        component.fileSizeLoading = false;
        fixture.detectChanges();
        const handleClick = spyOn(component, 'handleClickAttachment');

        const button: HTMLButtonElement = fixture.nativeElement.querySelector('.download-attachment-button');
        expect(button.tagName).toBe('BUTTON');
        expect(button.getAttribute('type')).toBe('button');
        expect(button.getAttribute('aria-label')).toBe('common.telechargerFichierNomme');
        expect(button.querySelector('mat-icon').getAttribute('aria-hidden')).toBe('true');
        expect(fixture.nativeElement.querySelector('.attachment-icon').getAttribute('aria-hidden')).toBe('true');
        button.click();
        expect(handleClick).toHaveBeenCalled();
    });
});
