import { Component, OnInit, Input, OnChanges, SimpleChanges } from '@angular/core';

import { IParsedATC } from './real-time-chart.component';

@Component({
    selector: 'pp-real-time-total-chart',
    templateUrl: './real-time-total-chart.component.html',
    styleUrls: ['./real-time-total-chart.component.css']
})
export class RealTimeTotalChartComponent implements OnChanges, OnInit {
    @Input() timezone: string;
    @Input() dateFormat: string;
    @Input() applicationName: string;
    @Input() timeStamp: number;
    @Input() sum: number[];
    @Input() activeThreadCounts: {[key: string]: IParsedATC};

    maxChartNumberPerPage = 1;
    chartOption = {
        canvasLeftPadding: 0,
        canvasTopPadding: 0,
        canvasRightPadding: 0,
        canvasBottomPadding: 0,
        chartInnerPadding: 15,
        containerWidth: 277,
        containerHeight: 144,
        chartWidth: 159,
        chartHeight: 114,
        titleHeight: 34,
        gapBtnChart: 0,
        chartColors: ['#33b692', '#51afdf', '#fea63e', '#e76f4b'],
        chartLabels: ['1s', '3s', '5s', 'Slow'],
        ellipsis: '...',
        drawHGridLine: false,
        drawVGridLine: true,
        showXAxis: true,
        showXAxisLabel: false,
        showYAxis: true,
        showYAxisLabel: true,
        yAxisLabelWidth: 8,
        marginFromYAxis: 5, // Space between y axis and y labels
        marginFromLegend: 10,
        tooltipEnabled: true,
        titleFontSize: '13px',
        errorFontSize: '15px',
        duration: 4000,
    };
    legendStyle: {right: string, top: string};
    totalCount: number;

    constructor() {}
    ngOnInit() {
        const {chartInnerPadding, titleHeight} = this.chartOption;

        this.legendStyle = {
            right: `${chartInnerPadding}px`,
            top: `${titleHeight + chartInnerPadding - 5}px`
        };
    }

    ngOnChanges(changes: SimpleChanges) {
        const sumChange = changes['sum'];

        if (sumChange) {
            this.totalCount = (sumChange.currentValue as number[]).reduce((acc: number, curr: number) => acc + curr, 0);
        }
    }
}
