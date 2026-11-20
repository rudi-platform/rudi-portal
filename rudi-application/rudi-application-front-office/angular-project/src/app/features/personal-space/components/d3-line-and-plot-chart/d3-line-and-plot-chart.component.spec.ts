import {ComponentFixture, TestBed} from '@angular/core/testing';
import {TpbcDataInterface} from '@core/services/selfdata-dataset/tpbcData.interface';
import {BarChartType} from 'micro_service_modules/selfdata/selfdata-api';

import {D3LineAndPlotChartComponent} from './d3-line-and-plot-chart.component';

describe('D3LineAndPlotChartComponent', () => {
    let component: D3LineAndPlotChartComponent;
    let fixture: ComponentFixture<D3LineAndPlotChartComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [D3LineAndPlotChartComponent]
        })
            .compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(D3LineAndPlotChartComponent);
        component = fixture.componentInstance;
        fixture.componentRef.setInput('graphBar', {
            type: BarChartType.Line,
            xLabels: ['Janvier'],
            lineAndPlot: [{color: '#000000', values: [{coordonateX: 'Janvier', coordonateY: 1}]}],
        } as TpbcDataInterface);
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
