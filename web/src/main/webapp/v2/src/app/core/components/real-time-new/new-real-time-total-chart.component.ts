import { Component, OnInit, Input } from '@angular/core';

import { IActiveThreadCounts, ResponseCode } from 'app/core/components/real-time-new/new-real-time-websocket.service';

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
        const successDataList = this.getSuccessDataList(activeThreadCounts);
        const hasError = successDataList.length === 0;

        this._activeThreadCounts = {
            [this.applicationName]: {
                code: hasError ? ResponseCode.ERROR_BLACK : ResponseCode.SUCCESS,
                message: hasError ? activeThreadCounts[Object.keys(activeThreadCounts)[0]].message : 'OK',
                status: hasError ? null : this.getTotalResponseCount(successDataList)
            }
        };

        this.data = hasError ? [] : this._activeThreadCounts[Object.keys(this._activeThreadCounts)[0]].status;
        this.totalCount = hasError ? null : this.data.reduce((acc: number, curr: number) => acc + curr, 0);
    }

    get activeThreadCounts(): { [key: string]: IActiveThreadCounts } {
        return this._activeThreadCounts;
    }

    _activeThreadCounts: { [key: string]: IActiveThreadCounts };
    data: number[];
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
        gridLineSpeedControl: 24,
        chartSpeedControl: 20,
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
        errorFontSize: '15px'
    };

    constructor() {}
    ngOnInit() {}

    private getSuccessDataList(obj: { [key: string]: IActiveThreadCounts }): number[][] {
        return Object.keys(obj)
            .filter((agentName: string) => obj[agentName].code === ResponseCode.SUCCESS)
            .map((agentName: string) => obj[agentName].status);
    }

    private getTotalResponseCount(dataList: number[][]): number[] {
        return dataList.reduce((acc: number[], curr: number[]) => {
            return acc.map((a: number, i: number) => a + curr[i]);
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
