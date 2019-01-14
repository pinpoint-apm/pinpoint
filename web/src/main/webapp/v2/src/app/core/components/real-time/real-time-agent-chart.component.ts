import { Component, OnInit, Input, Output, EventEmitter, AfterViewInit } from '@angular/core';

import { IActiveThreadCounts } from 'app/core/components/real-time/real-time-websocket.service';
import { ChartType } from './real-time-chart.component';

@Component({
    selector: 'pp-real-time-agent-chart',
    templateUrl: './real-time-agent-chart.component.html',
    styleUrls: ['./real-time-agent-chart.component.css']
})
export class RealTimeAgentChartComponent implements OnInit, AfterViewInit {
    @Input() timeStamp: number;
    @Input() activeThreadCounts: { [key: string]: IActiveThreadCounts };
    @Input() pagingSize: number;
    @Input() currentPage = 1;
    @Output() outOpenThreadDump = new EventEmitter<string>();
    @Output() outRenderCompleted = new EventEmitter<void>(true);

    chartOption = {
        canvasLeftPadding: 0,
        canvasTopPadding: 0,
        canvasRightPadding: 0,
        canvasBottomPadding: 0,
        chartInnerPadding: 0,
        containerWidth: 152,
        containerHeight: 52,
        chartWidth: 152,
        chartHeight: 52,
        titleHeight: 32,
        gapBtnChart: 10,
        chartColors: ['#33b692', '#51afdf', '#fea63e', '#e76f4b'],
        chartLabels: ['1s', '3s', '5s', 'Slow'],
        linkIconCode: '\uf35d',
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
        chartType: ChartType.EACH
    };

    constructor() {}
    ngOnInit() {}
    ngAfterViewInit() {
        this.outRenderCompleted.emit();
    }
    onClick(key: string): void {
        this.outOpenThreadDump.emit(key);
    }
}
