import {ComponentFixture, TestBed} from '@angular/core/testing';
import {MediaSize} from '@core/services/breakpoint-observer.service';

import {HeaderComponent} from '@shared/core/layout/header/header.component';

describe('AppHeaderComponent', () => {
    let component: HeaderComponent;
    let fixture: ComponentFixture<HeaderComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [HeaderComponent]
        })
            .compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(HeaderComponent);
        component = fixture.componentInstance;
        fixture.componentRef.setInput('mediaSize', {isDeviceMobile: false, isDeviceDesktop: true} as MediaSize);
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
