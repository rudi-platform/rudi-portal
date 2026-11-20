import {ComponentFixture, TestBed} from '@angular/core/testing';

import {
    DeletionConfirmationPopinComponent
} from '@shared/business/projects/projects-datasets-tables/deletion-confirmation-popin/deletion-confirmation-popin.component';

describe('DeletionConfirmationPopinComponent', () => {
    let component: DeletionConfirmationPopinComponent;
    let fixture: ComponentFixture<DeletionConfirmationPopinComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [DeletionConfirmationPopinComponent]
        })
            .compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(DeletionConfirmationPopinComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
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
