import {ComponentFixture, TestBed} from '@angular/core/testing';

import {
    SUCCESS_PROJECT_CREATION_DIALOG_TITLE_ID,
    SuccessProjectCreationDialogComponent
} from './success-project-creation-dialog.component';

describe('SuccessProjectCreationDialogComponent', () => {
    let fixture: ComponentFixture<SuccessProjectCreationDialogComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [SuccessProjectCreationDialogComponent]
        })
            .compileComponents();

        fixture = TestBed.createComponent(SuccessProjectCreationDialogComponent);
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(fixture.componentInstance).toBeTruthy();
    });

    it('should give the close button an accessible name (RGAA 11.9)', () => {
        const button: HTMLButtonElement = fixture.nativeElement.querySelector('.dialog-header-close-button button');
        expect(button.getAttribute('type')).toBe('button');
        expect(button.getAttribute('aria-label')).toBe('popup.close');
        expect(button.querySelector('mat-icon').getAttribute('aria-hidden')).toBe('true');
    });

    it('should expose the title id referenced by MatDialogConfig.ariaLabelledBy (RGAA 7.1)', () => {
        const title: HTMLElement = fixture.nativeElement.querySelector('h1.title');
        expect(title.id).toBe(SUCCESS_PROJECT_CREATION_DIALOG_TITLE_ID);
    });
});
