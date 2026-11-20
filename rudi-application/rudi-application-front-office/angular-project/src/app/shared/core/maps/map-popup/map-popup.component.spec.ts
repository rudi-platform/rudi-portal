import {ComponentFixture, TestBed} from '@angular/core/testing';
import {MapPopupComponent} from '@shared/core/maps/map-popup/map-popup.component';

describe('MapPopupComponent', () => {
    let component: MapPopupComponent;
    let fixture: ComponentFixture<MapPopupComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [MapPopupComponent]
        }).compileComponents();

        fixture = TestBed.createComponent(MapPopupComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should render the close control as a named native button with a hidden icon (RGAA 7.1, 11.9)', () => {
        const closeButton: HTMLButtonElement = fixture.nativeElement.querySelector('button.close-button');

        expect(closeButton).not.toBeNull();
        expect(closeButton.type).toBe('button');
        expect(closeButton.getAttribute('aria-label')).toBeTruthy();
        expect(closeButton.querySelector('mat-icon')?.getAttribute('aria-hidden')).toBe('true');
    });

    it('should emit closePopup when the close button is activated', () => {
        spyOn(component.closePopup, 'emit');

        fixture.nativeElement.querySelector('button.close-button').click();

        expect(component.closePopup.emit).toHaveBeenCalled();
    });
});
