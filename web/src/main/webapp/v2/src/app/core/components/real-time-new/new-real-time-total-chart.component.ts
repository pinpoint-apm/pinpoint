import { Component, OnInit, Input } from '@angular/core';

import { IActiveThreadCounts, ResponseCode } from 'app/core/components/real-time-new/new-real-time-websocket.service';
import { GridLineType } from './new-real-time-chart.component';

@Component({
    selector: 'pp-new-real-time-total-chart',
    templateUrl: './new-real-time-total-chart.component.html',
    styleUrls: ['./new-real-time-total-chart.component.css']
})
export class NewRealTimeTotalChartComponent implements OnInit {
    @Input() timeStamp: number;
    @Input() timezone: string;
    @Input() dateFormat: string;
    @Input() applicationName: string;
    @Input()
    set activeThreadCounts(activeThreadCounts: { [key: string]: IActiveThreadCounts }) {
        const successData = this.getSuccessData(activeThreadCounts);
        const hasError = successData.length === 0;

        this._activeThreadCounts = {
            [this.applicationName]: {
                code: hasError ? ResponseCode.ERROR_BLACK : ResponseCode.SUCCESS,
                message: hasError ? activeThreadCounts[Object.keys(activeThreadCounts)[0]].message : 'OK',
                status: hasError ? null : this.getTotalResponseCount(successData)
            }
        };

        this._data = this._activeThreadCounts[Object.keys(this._activeThreadCounts)[0]].status;
        this.totalCount = this._data ? this._data.reduce((acc: number, curr: number) => acc + curr, 0) : null;
    }

    get activeThreadCounts(): { [key: string]: IActiveThreadCounts } {
        return this._activeThreadCounts;
    }

    get data(): number[] {
        return this._data ? this._data : [];
    }

    _activeThreadCounts: { [key: string]: IActiveThreadCounts };
    _data: number[] = [];
    totalCount: number;

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
        chartSpeedControl: 20,
        ellipsis: '...',
        gridLineType: GridLineType.HORIZONTAL,
        showXAxis: true,
        showXAxisLabel: false,
        showYAxis: false,
        showYAxisLabel: true,
        yAxisWidth: 8,
        marginFromYAxis: 5,
        marginFromLegend: 10,
        tooltipEnabled: true,
        titleFontSize: '15px',
        errorFontSize: '15px'
    };

    constructor() {}
    ngOnInit() {}

    private getSuccessData(obj: { [key: string]: IActiveThreadCounts }): IActiveThreadCounts[] {
        return Object.keys(obj)
            .filter((agentName: string) => obj[agentName].code === ResponseCode.SUCCESS)
            .map((agentName: string) => obj[agentName]);
    }
    private getTotalResponseCount(data: IActiveThreadCounts[]): number[] {
        return data.reduce((prev: number[], curr: IActiveThreadCounts) => {
            return prev.map((a: number, i: number) => a + curr.status[i]);
        }, [0, 0, 0, 0]);
    }

    getLegendStyle(legend: HTMLElement): { [key: string]: string } {
        const { containerWidth, chartInnerPadding, titleHeight } = this.chartOption;
        const legendWidth = legend.offsetWidth;

        return {
            left: `${containerWidth - chartInnerPadding - legendWidth}px`,
            top: `${titleHeight + chartInnerPadding - 12}px`
        };
    }
}
