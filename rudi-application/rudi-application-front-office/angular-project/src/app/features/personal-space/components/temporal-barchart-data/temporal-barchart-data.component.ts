
import {Component, Input, OnInit} from '@angular/core';
import {BarchartService} from '@core/services/selfdata-dataset/barchart.service';
import {BarChartData, TpbcDataInterface} from '@core/services/selfdata-dataset/tpbcData.interface';
import {CardComponent} from '@shared/core/common/card/card.component';
import {LoaderComponent} from '@shared/core/common/loader/loader.component';
import {BarChartType} from 'micro_service_modules/selfdata/selfdata-api';
import {D3BarChartComponent} from '../d3-bar-chart/d3-bar-chart.component';
import {D3LineAndPlotChartComponent} from '../d3-line-and-plot-chart/d3-line-and-plot-chart.component';

@Component({
    selector: 'app-temporal-barchart-data',
    templateUrl: './temporal-barchart-data.component.html',
    styleUrls: ['./temporal-barchart-data.component.scss'],
    imports: [CardComponent, LoaderComponent, D3LineAndPlotChartComponent, D3BarChartComponent]
})
export class TemporalBarchartDataComponent implements OnInit {
    @Input() isLoading: boolean;
    @Input() barChartData: BarChartData;
    graphData: TpbcDataInterface;

    constructor(private readonly barchartService: BarchartService) {
    }

    get isLineOrPoint(): boolean {
        return this.barChartData.type === BarChartType.Line || this.barChartData.type === BarChartType.Point;
    }

    get isBar(): boolean {
        return this.barChartData.type === BarChartType.Bar;
    }


    ngOnInit(): void {
        this.graphData = this.barchartService.getDataGraph(this.barChartData);
    }

}
