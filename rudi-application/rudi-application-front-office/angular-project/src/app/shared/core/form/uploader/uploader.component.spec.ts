import {ComponentFixture, TestBed} from '@angular/core/testing';
import {FormControl} from '@angular/forms';
import {FilePickerModule} from '@sleiss/ngx-awesome-uploader';

import {UploaderComponent} from '@shared/core/form/uploader/uploader.component';

describe('UploaderComponent', () => {
    let component: UploaderComponent<unknown>;
    let fixture: ComponentFixture<UploaderComponent<unknown>>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [UploaderComponent, FilePickerModule]
        })
            .compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(UploaderComponent);
        component = fixture.componentInstance;
        fixture.componentRef.setInput('fileFormControl', new FormControl(null));
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should include the visible dropzone text in the accessible name (WCAG 2.5.3)', () => {
        fixture.componentRef.setInput('ariaLabelledby', 'field-label');
        fixture.detectChanges();
        const dropzone: HTMLElement = fixture.nativeElement.querySelector('.block-upload');
        const ids = dropzone.getAttribute('aria-labelledby').split(' ');
        expect(ids[0]).toBe('field-label');
        expect(fixture.nativeElement.querySelector(`#${ids[1]}`)?.classList).toContain('ajouter-une-image');
        expect(fixture.nativeElement.querySelector(`#${ids[2]}`)?.classList).toContain('au-format-png-pdf-o');
    });

    it('should keep the aria-label when no label id is given', () => {
        fixture.componentRef.setInput('imageText', 'uploader.text');
        fixture.detectChanges();
        const dropzone: HTMLElement = fixture.nativeElement.querySelector('.block-upload');
        expect(dropzone.hasAttribute('aria-labelledby')).toBeFalse();
        expect(dropzone.getAttribute('aria-label')).toBeTruthy();
    });
});
