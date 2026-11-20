import {ComponentFixture, TestBed} from '@angular/core/testing';
import {MediaSize} from '@core/services/breakpoint-observer.service';

import {BannerComponent} from './banner.component';

describe('BannerComponent', () => {
    let component: BannerComponent;
    let fixture: ComponentFixture<BannerComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [BannerComponent]
        })
            .compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(BannerComponent);
        component = fixture.componentInstance;
        fixture.componentRef.setInput('mediaSize', {isLg: true} as MediaSize);
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
