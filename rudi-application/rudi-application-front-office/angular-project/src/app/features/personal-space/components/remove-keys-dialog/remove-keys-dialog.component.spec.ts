import {ComponentFixture, TestBed} from '@angular/core/testing';

import {RemoveKeysDialogComponent} from './remove-keys-dialog.component';

describe('RemoveKeysDialogComponent', () => {
    let fixture: ComponentFixture<RemoveKeysDialogComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [RemoveKeysDialogComponent]
        })
            .compileComponents();

        fixture = TestBed.createComponent(RemoveKeysDialogComponent);
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(fixture.componentInstance).toBeTruthy();
    });

    it('should give the close button an accessible name (RGAA 11.9)', () => {
        const button: HTMLButtonElement = fixture.nativeElement.querySelector('.dialog-cross-button button');
        expect(button.getAttribute('type')).toBe('button');
        expect(button.getAttribute('aria-label')).toBe('popup.close');
        expect(button.querySelector('mat-icon').getAttribute('aria-hidden')).toBe('true');
    });

    it('should name the dialog by its visible title (RGAA 7.1)', () => {
        const title: HTMLElement = fixture.nativeElement.querySelector('.dialog-title');
        expect(title.classList).toContain('mat-mdc-dialog-title');
        expect(title.id).toMatch(/^mat-mdc-dialog-title-/);
    });
});
