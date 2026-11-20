import {ComponentFixture, TestBed} from '@angular/core/testing';
import {TpbcDataInterface} from '@core/services/selfdata-dataset/tpbcData.interface';

import {D3BarChartComponent} from './d3-bar-chart.component';

describe('D3BarChartComponent', () => {
    let component: D3BarChartComponent;
    let fixture: ComponentFixture<D3BarChartComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [D3BarChartComponent]
        })
            .compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(D3BarChartComponent);
        component = fixture.componentInstance;
        fixture.componentRef.setInput('graphBar', {
            xLabels: ['Janvier'],
            groupedModels: [{coordonateX: 'Janvier', serie1: 2}],
            colors: ['#000000'],
        } as TpbcDataInterface);
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
