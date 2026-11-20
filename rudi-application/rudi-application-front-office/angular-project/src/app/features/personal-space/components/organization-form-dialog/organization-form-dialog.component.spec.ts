import {ComponentFixture, TestBed} from '@angular/core/testing';
import {OrganizationFormDialogComponent} from './organization-form-dialog.component';

describe('OrganizationFormDialogComponent', () => {
    let component: OrganizationFormDialogComponent;
    let fixture: ComponentFixture<OrganizationFormDialogComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [OrganizationFormDialogComponent]
        }).compileComponents();

        fixture = TestBed.createComponent(OrganizationFormDialogComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should keep the confirm button enabled while the form is invalid (RGAA 11.10)', () => {
        const buttons: HTMLButtonElement[] = Array.from(fixture.nativeElement.querySelectorAll('mat-dialog-actions button'));

        expect(buttons.length).toBe(2);
        buttons.forEach(button => {
            expect(button.disabled).toBeFalse();
            expect(button.type).toBe('button');
        });
    });

    it('should focus the first invalid field when confirm fails', () => {
        spyOn(component.organizationFormComponent, 'submitWorkflowForm').and.returnValue(false);
        const getOrganization = spyOn(component.organizationFormComponent, 'getOrganization$');
        const invalidField = document.createElement('input');
        invalidField.classList.add('ng-invalid');
        fixture.nativeElement.appendChild(invalidField);

        component.onClickConfirm();

        expect(document.activeElement).toBe(invalidField);
        expect(getOrganization).not.toHaveBeenCalled();
    });
});
