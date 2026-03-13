import {Component, forwardRef, ViewEncapsulation} from '@angular/core';
import {ControlValueAccessor, FormsModule, NG_VALUE_ACCESSOR} from '@angular/forms';
import {ChangeEvent, CKEditorModule} from '@ckeditor/ckeditor5-angular';
import {
    Alignment,
    Autoformat,
    AutoLink,
    Autosave,
    BlockQuote,
    Bold,
    ClassicEditor,
    Code,
    Essentials,
    HorizontalLine,
    Italic,
    Link,
    List,
    Paragraph,
    SourceEditing,
    Strikethrough,
    TextPartLanguage,
    TextTransformation,
    TodoList,
    Underline,
    type EditorConfig,
} from 'ckeditor5';
// ckeditor5 translations are shipped as JS-only modules (no TS typings).
// We keep the import explicit so the bundler includes the French UI strings.
// @ts-ignore
import fr from 'ckeditor5/translations/fr.js';

@Component({
    selector: 'app-rich-text-editor',
    standalone: true,
    imports: [CKEditorModule, FormsModule],
    templateUrl: './rich-text-editor.component.html',
    styleUrls: ['./rich-text-editor.component.scss'],
    encapsulation: ViewEncapsulation.None,
    providers: [
        {
            provide: NG_VALUE_ACCESSOR,
            useExisting: forwardRef(() => RichTextEditorComponent),
            multi: true,
        },
    ],
})
export class RichTextEditorComponent implements ControlValueAccessor {
    public editor = ClassicEditor;
    public data = '';
    public isDisabled = false;

    public config: EditorConfig = {
        licenseKey: 'GPL',
        initialData: '',
        language: {
            ui: 'fr',
            content: 'fr',
        },
        plugins: [
            Alignment,
            Autoformat,
            AutoLink,
            Autosave,
            BlockQuote,
            Bold,
            Code,
            Essentials,
            HorizontalLine,
            Italic,
            Link,
            List,
            Paragraph,
            SourceEditing,
            Strikethrough,
            TextPartLanguage,
            TextTransformation,
            TodoList,
            Underline,
        ],
        toolbar: [
            'undo',
            'redo',
            '|',
            'sourceEditing',
            '|',
            'bold',
            'italic',
            'underline',
            '|',
            'link',
            '|',
            'alignment',
            '|',
            'bulletedList',
            'numberedList',
        ],
        translations: fr,
    };

    // Callbacks pour Angular Forms
    private onChange = (_value: string) => {};
    private onTouched = () => {};

    /**
     * Appelé par Angular quand la valeur change dans le formulaire (Project -> Editor)
     */
    writeValue(value: string): void {
        this.data = value || '';
    }

    /**
     * Enregistre la fonction de callback pour notifier Angular d'un changement (Editor -> Project)
     */
    registerOnChange(fn: any): void {
        this.onChange = fn;
    }

    /**
     * Enregistre la fonction de callback pour notifier Angular que le champ a été touché
     */
    registerOnTouched(fn: any): void {
        this.onTouched = fn;
    }

    setDisabledState(isDisabled: boolean): void {
        this.isDisabled = isDisabled;
    }

    /**
     * Appelé à chaque modification dans l'éditeur
     */
    public onEditorChange({ editor }: ChangeEvent): void {
        const value = editor.getData();
        this.onChange(value);
    }

    public onEditorBlur(): void {
        this.onTouched();
    }
}
