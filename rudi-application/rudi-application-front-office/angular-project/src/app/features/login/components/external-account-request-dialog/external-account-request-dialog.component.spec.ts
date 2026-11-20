import {ComponentFixture, TestBed} from '@angular/core/testing';
import {MAT_DIALOG_DATA} from '@angular/material/dialog';

import {ExternalAccountRequestDialogComponent} from './external-account-request-dialog.component';

describe('ExternalAccountRequestDialogComponent', () => {
    let fixture: ComponentFixture<ExternalAccountRequestDialogComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [ExternalAccountRequestDialogComponent],
            providers: [{
                provide: MAT_DIALOG_DATA,
                useValue: {authenticators: [{name: 'test', label: 'Test', supportAccountCreation: true}], enableCaptcha: false}
            }]
        })
            .compileComponents();

        fixture = TestBed.createComponent(ExternalAccountRequestDialogComponent);
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(fixture.componentInstance).toBeTruthy();
    });

    it('should give the close button an accessible name (RGAA 11.9)', () => {
        const button: HTMLButtonElement = fixture.nativeElement.querySelector('.dialog-cross-button button');
        expect(button.getAttribute('type')).toBe('button');
        expect(button.getAttribute('aria-label')).toBe('externalAccountRequest.close');
        expect(button.querySelector('mat-icon').getAttribute('aria-hidden')).toBe('true');
    });

    it('should name the dialog by its visible title (RGAA 7.1)', () => {
        const title: HTMLElement = fixture.nativeElement.querySelector('h2.dialog-title');
        expect(title.classList).toContain('mat-mdc-dialog-title');
        expect(title.id).toMatch(/^mat-mdc-dialog-title-/);
    });
});
