import {ComponentFixture, TestBed} from '@angular/core/testing';

import {RichTextEditorComponent} from '@shared/core/common/rich-text-editor/rich-text-editor.component';

describe('RichTextEditorComponent', () => {
    let fixture: ComponentFixture<RichTextEditorComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [RichTextEditorComponent]
        })
            .compileComponents();
    });

    afterEach(() => fixture?.destroy());

    // CKEditor is created asynchronously outside the Angular zone, so whenStable() does not wait for it.
    async function renderEditable(ariaRequired: boolean, inputs: Record<string, string> = {}): Promise<HTMLElement> {
        fixture = TestBed.createComponent(RichTextEditorComponent);
        fixture.componentRef.setInput('ariaRequired', ariaRequired);
        Object.entries(inputs).forEach(([name, value]) => fixture.componentRef.setInput(name, value));
        fixture.detectChanges();
        const readySpy = spyOn(fixture.componentInstance, 'onEditorReady').and.callThrough();
        for (let i = 0; i < 100 && readySpy.calls.count() === 0; i++) {
            await new Promise(resolve => setTimeout(resolve, 50));
        }
        expect(readySpy).toHaveBeenCalled();
        return fixture.nativeElement.querySelector('.ck-editor__editable');
    }

    it('should expose the required state on the editable area (RGAA 11.10)', async () => {
        const editable = await renderEditable(true);
        expect(editable.getAttribute('aria-required')).toBe('true');
    });

    it('should not expose a required state by default', async () => {
        const editable = await renderEditable(false);
        expect(editable.hasAttribute('aria-required')).toBeFalse();
    });

    it('should name the editable area with the visible label and hint (RGAA 11.1)', async () => {
        const editable = await renderEditable(false, {ariaLabelledby: 'field-label', ariaDescribedby: 'field-hint'});
        expect(editable.getAttribute('aria-labelledby')).toBe('field-label');
        expect(editable.getAttribute('aria-describedby')).toBe(`field-hint ${fixture.componentInstance.shortcutsHintId}`);
    });

    it('should not reference a label by default', async () => {
        const editable = await renderEditable(false);
        expect(editable.hasAttribute('aria-labelledby')).toBeFalse();
    });

    it('should tell how to reach the toolbar, which is out of the tab order (RGAA 7.1)', async () => {
        const editable = await renderEditable(false);
        const hintId = fixture.componentInstance.shortcutsHintId;
        expect(editable.getAttribute('aria-describedby')).toBe(hintId);
        const hint: HTMLElement = fixture.nativeElement.querySelector(`#${hintId}`);
        expect(hint.classList).toContain('visually-hidden');
        expect(hint.textContent).toContain('common.richTextEditorShortcuts');
    });
});
