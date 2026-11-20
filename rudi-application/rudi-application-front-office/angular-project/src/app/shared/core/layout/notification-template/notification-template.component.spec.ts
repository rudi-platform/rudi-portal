import {ComponentFixture, TestBed} from '@angular/core/testing';
import {MAT_SNACK_BAR_DATA, MatSnackBarRef} from '@angular/material/snack-bar';

import {Level, NotificationTemplateComponent} from '@shared/core/layout/notification-template/notification-template.component';

describe('NotificationTemplateComponent', () => {
    let component: NotificationTemplateComponent;
    let fixture: ComponentFixture<NotificationTemplateComponent>;
    let snackBarRef: jasmine.SpyObj<MatSnackBarRef<NotificationTemplateComponent>>;

    beforeEach(async () => {
        snackBarRef = jasmine.createSpyObj('MatSnackBarRef', ['dismiss']);
        await TestBed.configureTestingModule({
            imports: [NotificationTemplateComponent],
            providers: [
                {provide: MAT_SNACK_BAR_DATA, useValue: {message: 'Erreur', level: Level.ERROR}},
                {provide: MatSnackBarRef, useValue: snackBarRef},
            ]
        })
            .compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(NotificationTemplateComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should hide the decorative icons (RGAA 1.2)', () => {
        const icons: HTMLElement[] = Array.from(fixture.nativeElement.querySelectorAll('em'));
        expect(icons.length).toBe(2);
        icons.forEach(icon => expect(icon.getAttribute('aria-hidden')).withContext(icon.className).toBe('true'));
    });

    it('should close through a named native button (RGAA 7.3 / 11.9)', () => {
        const button: HTMLButtonElement = fixture.nativeElement.querySelector('button');
        expect(button.type).toBe('button');
        expect(button.getAttribute('aria-label')).toBe('popup.close');
        button.click();
        expect(snackBarRef.dismiss).toHaveBeenCalled();
    });
});
