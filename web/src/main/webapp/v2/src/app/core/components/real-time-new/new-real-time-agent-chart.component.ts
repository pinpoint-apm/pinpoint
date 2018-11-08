import { Component, OnInit, Input, Output, EventEmitter, ViewChild, ElementRef } from '@angular/core';

import { IActiveThreadCounts } from 'app/core/components/real-time-new/new-real-time-websocket.service';
import { GridLineType } from './new-real-time-chart.component';

@Component({
    selector: 'pp-new-real-time-agent-chart',
    templateUrl: './new-real-time-agent-chart.component.html',
    styleUrls: ['./new-real-time-agent-chart.component.css']
})
export class NewRealTimeAgentChartComponent implements OnInit {
    @Input() timeStamp: number;
    @Input()
    set activeThreadCounts(activeThreadCounts: { [key: string]: IActiveThreadCounts }) {
        this._activeThreadCounts = activeThreadCounts;
        this._dataList = Object.keys(activeThreadCounts).map((key: string, i: number) => {
            const status = activeThreadCounts[key].status;

            return status ?
                this._dataList[i] ? this._dataList[i].map((data: number[], j: number) => [ ...data, status[j] ]) : status.map((v: number) => [v]) :
                [ [], [], [], [] ];
        });
    }

    get activeThreadCounts(): { [key: string]: IActiveThreadCounts } {
        return this._activeThreadCounts;
    }

    get dataList(): number[][][] {
        return this._dataList;
    }

    @Output() outOpenThreadDump: EventEmitter<string> = new EventEmitter();
    @ViewChild('canvas') canvasRef: ElementRef;

    _activeThreadCounts: { [key: string]: IActiveThreadCounts };
    _dataList: number[][][] = [];

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
        gridLineSpeedControl: 25,
        chartSpeedControl: 25,
        linkIconCode: '\uf35d',
        marginRightForLinkIcon: 10,
        ellipsis: '...',
        gridLineType: GridLineType.VERTICAL,
        showXAxis: false,
        showXAxisLabel: false,
        showYAxis: false,
        showYAxisLabel: false,
        yAxisWidth: 0,
        marginFromYAxis: 0,
        tooltipEnabled: false,
        titleFontSize: '11px',
        errorFontSize: '13px'
    };

    constructor() {}
    ngOnInit() {}
    onClick(key: string): void {
        this.outOpenThreadDump.emit(key);
    }
}
