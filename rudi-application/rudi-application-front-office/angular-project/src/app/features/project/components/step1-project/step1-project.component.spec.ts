import {provideHttpClient} from '@angular/common/http';
import {provideHttpClientTesting} from '@angular/common/http/testing';
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {FormControl, FormGroup, Validators} from '@angular/forms';
import {By} from '@angular/platform-browser';
import {ProjektMetierService} from '@core/services/asset/project/projekt-metier.service';
import {RichTextEditorComponent} from '@shared/core/common/rich-text-editor/rich-text-editor.component';
import {provideTranslateService} from '@ngx-translate/core';
import {NEVER} from 'rxjs';

import {Step1ProjectComponent} from './step1-project.component';

describe('Step1ProjectComponent', () => {
    let fixture: ComponentFixture<Step1ProjectComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [Step1ProjectComponent],
            providers: [
                provideHttpClient(),
                provideHttpClientTesting(),
                provideTranslateService(),
                {provide: ProjektMetierService, useValue: {getDataSizeProperty: () => NEVER, getStrings: () => NEVER}}
            ]
        })
            .compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(Step1ProjectComponent);
        // Same required controls as ProjectSubmissionService.initStep1ProjectFormGroup()
        fixture.componentRef.setInput('step1FormGroup', new FormGroup({
            title: new FormControl('', Validators.required),
            description: new FormControl('', Validators.required),
            image: new FormControl(''),
            begin_date: new FormControl(null),
            end_date: new FormControl(null),
            publicCible: new FormControl(''),
            echelle: new FormControl(null),
            territoire: new FormControl(null),
            accompagnement: new FormControl(null),
            type: new FormControl('', Validators.required),
            url: new FormControl(''),
            reuse_status: new FormControl('', Validators.required),
            confidentiality: new FormControl('', Validators.required)
        }));
        fixture.componentRef.setInput('isConfidentialityValid', true);
        fixture.componentRef.setInput('suggestions', [{code: 'OPEN', label: 'Open', description: 'Open project'}]);
        fixture.detectChanges();
    });

    it('should name every select by its visible label (RGAA 11.1)', () => {
        const selects: HTMLElement[] = Array.from(fixture.nativeElement.querySelectorAll('mat-select'));
        expect(selects.length).toBe(5);
        selects.forEach(select => {
            const label: HTMLLabelElement = fixture.nativeElement.querySelector(`label[for="${select.id}"]`);
            expect(label?.id).withContext(select.id).toBeTruthy();
            expect(select.getAttribute('aria-labelledby').trim().split(/\s+/)).withContext(select.id).toContain(label.id);
        });
    });

    it('should name the description editor by its visible label (RGAA 11.1)', () => {
        const editor: RichTextEditorComponent = fixture.debugElement.query(By.directive(RichTextEditorComponent)).componentInstance;
        const label: HTMLLabelElement = fixture.nativeElement.querySelector(`label#${editor.ariaLabelledby}`);
        expect(label?.getAttribute('for')).toBe('project-description');
    });

    it('should mark the confidentiality radios as required (RGAA 11.10)', () => {
        const radios: HTMLInputElement[] = Array.from(fixture.nativeElement.querySelectorAll('app-radio-list input[type="radio"]'));
        expect(radios.length).toBe(1);
        radios.forEach(radio => expect(radio.required).toBeTrue());
    });

    it('should name the confidentiality radio group by its visible label and describe it by its hint (RGAA 11.5, 11.6)', () => {
        const group: HTMLElement = fixture.nativeElement.querySelector('mat-radio-group');
        expect(group.getAttribute('aria-labelledby')).toBe('project-confidentiality-label');
        expect(group.getAttribute('aria-describedby').split(' ')).toContain('project-confidentiality-hint');
        expect(fixture.nativeElement.querySelector('#project-confidentiality-hint')).toBeTruthy();
    });

    it('should not expose the field labels as groups (RGAA 11.5)', () => {
        ['project-periode-label', 'project-confidentiality-label'].forEach(id => {
            const label: HTMLElement = fixture.nativeElement.querySelector(`#${id}`);
            expect(label.getAttribute('role')).withContext(id).toBeNull();
            expect(label.getAttribute('aria-label')).withContext(id).toBeNull();
        });
    });

    it('should describe the description editor by its hint and its error (RGAA 11.10)', () => {
        const editor: RichTextEditorComponent = fixture.debugElement.query(By.directive(RichTextEditorComponent)).componentInstance;
        const ids = editor.ariaDescribedby.split(' ');
        expect(ids).toEqual(['project-description-hint', 'project-description-error']);
        ids.forEach(id => expect(fixture.nativeElement.querySelector(`#${id}`)).withContext(id).toBeTruthy());
    });

    it('should show every error and focus the first invalid field (RGAA 11.10)', () => {
        fixture.componentInstance.focusFirstInvalidField();

        expect(fixture.nativeElement.ownerDocument.activeElement.id).toBe('project-title');
        expect(fixture.nativeElement.querySelector('#project-description-error').textContent)
            .toContain('project.stepper.submission.step1.errorFieldRequired');
    });

    it('should focus the first invalid select when the text fields are valid (RGAA 11.10)', () => {
        const form = fixture.componentInstance.step1FormGroup;
        form.patchValue({title: 'Title', description: 'Description'});
        fixture.componentInstance.focusFirstInvalidField();

        expect(fixture.nativeElement.ownerDocument.activeElement.id).toBe('project-reuse-status');
    });
});
