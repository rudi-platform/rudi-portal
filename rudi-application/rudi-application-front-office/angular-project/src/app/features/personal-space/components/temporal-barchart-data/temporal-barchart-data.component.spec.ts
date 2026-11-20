import {ComponentFixture, TestBed} from '@angular/core/testing';
import {BarChartData} from '@core/services/selfdata-dataset/tpbcData.interface';
import {BarChartType} from 'micro_service_modules/selfdata/selfdata-api';

import {TemporalBarchartDataComponent} from './temporal-barchart-data.component';

describe('TemporalBarchartDataComponent', () => {
    let component: TemporalBarchartDataComponent;
    let fixture: ComponentFixture<TemporalBarchartDataComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [TemporalBarchartDataComponent]
        })
            .compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(TemporalBarchartDataComponent);
        component = fixture.componentInstance;
        fixture.componentRef.setInput('isLoading', true);
        fixture.componentRef.setInput('barChartData', {type: BarChartType.Bar, series: [], values: []} as BarChartData);
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
