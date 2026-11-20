import {ComponentFixture, TestBed} from '@angular/core/testing';

import {SelfdataDatasetsComponent} from './selfdata-datasets.component';

describe('SelfdataDatasetsComponent', () => {
    let component: SelfdataDatasetsComponent;
    let fixture: ComponentFixture<SelfdataDatasetsComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [SelfdataDatasetsComponent]
        })
            .compileComponents();
    });

    beforeEach(async () => {
        fixture = TestBed.createComponent(SelfdataDatasetsComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
        await fixture.whenStable();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
