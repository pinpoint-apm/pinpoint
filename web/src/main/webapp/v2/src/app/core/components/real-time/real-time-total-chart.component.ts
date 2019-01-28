import { Component, OnInit, Input } from '@angular/core';

import { IParsedATC } from './real-time-chart.component';

@Component({
    selector: 'pp-real-time-total-chart',
    templateUrl: './real-time-total-chart.component.html',
    styleUrls: ['./real-time-total-chart.component.css']
})
export class RealTimeTotalChartComponent implements OnInit {
    @Input() timezone: string;
    @Input() dateFormat: string;
    @Input() applicationName: string;
    @Input() timeStamp: number;
    @Input() sum: number[];
    @Input() activeThreadCounts: { [key: string]: IParsedATC };

    maxChartNumberPerPage = 1;
    chartOption = {
        canvasLeftPadding: 0,
        canvasTopPadding: 0,
        canvasRightPadding: 0,
        canvasBottomPadding: 0,
        chartInnerPadding: 15,
        containerWidth: 277,
        containerHeight: 132,
        chartWidth: 159,
        chartHeight: 102,
        titleHeight: 46,
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
        titleFontSize: '15px',
        errorFontSize: '15px',
        duration: 4000,
    };

    constructor() {}
    ngOnInit() {}
    getLegendStyle(legend: HTMLElement): { [key: string]: string } {
        const { containerWidth, chartInnerPadding, titleHeight } = this.chartOption;
        const legendWidth = legend.offsetWidth;

        return {
            left: `${containerWidth - chartInnerPadding - legendWidth}px`,
            top: `${titleHeight + chartInnerPadding - 12}px`
        };
    }

    getTotalCount(): number {
        return this.sum.reduce((acc: number, curr: number) => acc + curr, 0);
    }
}
