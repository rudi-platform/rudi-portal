import {ComponentFixture, TestBed} from '@angular/core/testing';

import {DataSetCardComponent} from '@shared/business/dataset/common/data-set-card/data-set-card.component';

describe('DataSetCardComponent', () => {
    let component: DataSetCardComponent;
    let fixture: ComponentFixture<DataSetCardComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [DataSetCardComponent]
        })
            .compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(DataSetCardComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
