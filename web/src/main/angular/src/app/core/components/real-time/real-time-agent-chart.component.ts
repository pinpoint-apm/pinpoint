import { Component, OnInit, Input, Output, EventEmitter, AfterViewInit } from '@angular/core';

import { IParsedARC } from './real-time-chart.component';

@Component({
    selector: 'pp-real-time-agent-chart',
    templateUrl: './real-time-agent-chart.component.html',
    styleUrls: ['./real-time-agent-chart.component.css']
})
export class RealTimeAgentChartComponent implements OnInit, AfterViewInit {
    @Input() timeStamp: number;
    @Input() activeRequestCounts: { [key: string]: IParsedARC };
    @Input() currentPage = 1;
    @Input() sum: number[];
    @Output() outOpenThreadDump = new EventEmitter<string>();
    @Output() outRenderCompleted = new EventEmitter<void>(true);

    computedStyle = getComputedStyle(document.body);
    chartColor = {
        one: this.computedStyle.getPropertyValue('--chart-1s'),
        three: this.computedStyle.getPropertyValue('--chart-3s'),
        five: this.computedStyle.getPropertyValue('--chart-5s'),
        slow: this.computedStyle.getPropertyValue('--chart-slow'),
    };

    maxChartNumberPerPage = 30;
    chartOption = {
        canvasLeftPadding: 0,
        canvasTopPadding: 0,
        canvasRightPadding: 0,
        canvasBottomPadding: 0,
        chartInnerPadding: 0,
        containerWidth: 152,
        containerHeight: 60,
        chartWidth: 152,
        chartHeight: 60,
        titleHeight: 24,
        gapBtnChart: 5,
        chartColors: [this.chartColor.one, this.chartColor.three, this.chartColor.five, this.chartColor.slow],
        chartLabels: ['1s', '3s', '5s', 'Slow'],
        linkIconCode: '\uf002',
        marginRightForLinkIcon: 10,
        ellipsis: '...',
        drawHGridLine: false,
        drawVGridLine: true,
        showXAxis: false,
        showXAxisLabel: false,
        showYAxis: false,
        showYAxisLabel: false,
        yAxisLabelWidth: 0,
        marginFromYAxis: 0,
        tooltipEnabled: false,
        titleFontSize: '11px',
        errorFontSize: '13px',
        duration: 4000,
    };

    constructor() {}
    ngOnInit() {}
    ngAfterViewInit() {
        this.outRenderCompleted.emit();
    }

    onOpenThreadDump(agentId: string): void {
        this.outOpenThreadDump.emit(agentId);
    }
}
