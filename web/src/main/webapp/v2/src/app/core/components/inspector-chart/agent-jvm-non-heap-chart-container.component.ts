import { Component, OnInit, OnDestroy, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import * as moment from 'moment-timezone';

import { Actions } from 'app/shared/store';
import { WebAppSettingDataService, NewUrlStateNotificationService, AjaxExceptionCheckerService, AnalyticsService, StoreHelperService, DynamicPopupService } from 'app/shared/services';
import { AgentMemoryChartDataService } from './agent-memory-chart-data.service';
import { HELP_VIEWER_LIST } from 'app/core/components/help-viewer-popup/help-viewer-popup-container.component';
import { InspectorChartContainer } from 'app/core/components/inspector-chart/inspector-chart-container';
import { IChartDataFromServer } from 'app/core/components/inspector-chart/chart-data.service';

@Component({
    selector: 'pp-agent-jvm-non-heap-chart-container',
    templateUrl: './agent-jvm-non-heap-chart-container.component.html',
    styleUrls: ['./agent-jvm-non-heap-chart-container.component.css'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class AgentJVMNonHeapChartContainerComponent extends InspectorChartContainer implements OnInit, OnDestroy {
    constructor(
        storeHelperService: StoreHelperService,
        changeDetector: ChangeDetectorRef,
        webAppSettingDataService: WebAppSettingDataService,
        newUrlStateNotificationService: NewUrlStateNotificationService,
        chartDataService: AgentMemoryChartDataService,
        translateService: TranslateService,
        ajaxExceptionCheckerService: AjaxExceptionCheckerService,
        analyticsService: AnalyticsService,
        dynamicPopupService: DynamicPopupService
    ) {
        super(
            100,
            storeHelperService,
            changeDetector,
            webAppSettingDataService,
            newUrlStateNotificationService,
            chartDataService,
            translateService,
            ajaxExceptionCheckerService,
            analyticsService,
            dynamicPopupService
        );
    }

    ngOnInit() {
        this.initI18nText();
        this.initHoveredInfo();
        this.initTimezoneAndDateFormat();
        this.initChartData();
    }

    ngOnDestroy() {
        this.unsubscribe.next();
        this.unsubscribe.complete();
    }

    protected setChartConfig(data: {[key: string]: any}): void {
        this.chartConfig =  {
            type: 'bar',
            dataConfig: this.makeDataOption(data),
            elseConfig: this.makeNormalOption(data),
            isDataEmpty: this.isDataEmpty(data)
        };
        this.changeDetector.detectChanges();
    }

    protected makeChartData(chartData: IChartDataFromServer): {[key: string]: any} {
        const xArr = [];
        const maxArr = [];
        const usedArr = [];
        const fgcTimeArr = [];
        const fgcCountArr = [];

        const xData = chartData.charts.x;
        const gcOldTime = chartData.charts.y['JVM_GC_OLD_TIME'];
        const gcOldCount = chartData.charts.y['JVM_GC_OLD_COUNT'];
        const memoryUsed = chartData.charts.y['JVM_MEMORY_NON_HEAP_USED'];
        const memoryMax = chartData.charts.y['JVM_MEMORY_NON_HEAP_MAX'];
        const dataCount = xData.length;

        let totalSumGCTime = 0;
        for ( let i = 0 ; i < dataCount ; i++ ) {
            xArr.push(moment(xData[i]).tz(this.timezone).format(this.dateFormat[0]) + '#' + moment(xData[i]).tz(this.timezone).format(this.dateFormat[1]));
            if ( memoryMax.length  === 0 ) {
                continue;
            }
            maxArr.push(this.parseData(memoryMax[i][1]));
            usedArr.push(this.parseData(memoryUsed[i][1]));

            const gcOldCountSumValue = gcOldCount[i][3];
            const gcOldTimeSumValue = gcOldTime[i][3];

            if ( gcOldTimeSumValue > 0 ) {
                totalSumGCTime += gcOldTimeSumValue;
            }
            if ( gcOldCountSumValue > 0 ) {
                fgcTimeArr.push(totalSumGCTime);
                fgcCountArr.push(gcOldCountSumValue);
                totalSumGCTime = 0;
            } else {
                fgcTimeArr.push(0);
                fgcCountArr.push(0);
            }
        }
        return {
            x: xArr,
            max: maxArr,
            used: usedArr,
            fgcTime: fgcTimeArr,
            fgcCount: fgcCountArr
        };
    }

    protected makeDataOption(data: {[key: string]: any}): {[key: string]: any} {
        return {
            labels: data.x,
            datasets: [{
                type: 'line',
                label: 'Max',
                data: data.max,
                fill: false,
                borderWidth: 0.5,
                borderColor: 'rgb(174, 199, 232)',
                backgroundColor: 'rgb(174, 199, 232)',
                pointRadius: 0,
                pointHoverRadius: 3
            }, {
                type: 'line',
                label: 'Used',
                data: data.used,
                fill: true,
                borderWidth: 0.5,
                borderColor: 'rgb(31, 119, 180)',
                backgroundColor: 'rgba(31, 119, 180, 0.4)',
                pointRadius: 0,
                pointHoverRadius: 3
            }, {
                type: 'bar',
                label: 'Major GC',
                data: data.fgcTime,
                borderWidth: 1,
                borderColor: 'rgb(255, 42, 0)',
                backgroundColor: 'rgba(255, 42, 0, 0.3)',
                // pointRadius: 0,
                // pointHoverRadius: 3,
                yAxisID: 'y-axis-2'
            }]
        };
    }

    protected makeNormalOption(data: {[key: string]: any}): {[key: string]: any} {
        return {
            responsive: true,
            title: {
                display: false,
                text: 'Heap Usage'
            },
            tooltips: {
                mode: 'index',
                intersect: false,
                callbacks: {
                    title: (value: {[key: string]: any}[]): string => {
                        return value[0].xLabel.join(' ');
                    },
                    label: (value: {[key: string]: any}, d: {[key: string]: any}): string => {
                        return `${d.datasets[value.datasetIndex].label}: ${isNaN(value.yLabel) ? `-` : this.convertWithUnit(value.yLabel)}`;
                    }
                }
            },
            hover: {
                mode: 'index',
                intersect: false,
                onHover: (event: MouseEvent, elements: any[]): void => {
                    if (!this.isDataEmpty(data)) {
                        this.storeHelperService.dispatch(new Actions.ChangeHoverOnInspectorCharts({
                            index: event.type === 'mouseout' ? -1 : elements[0]._index,
                            offsetX: event.offsetX,
                            offsetY: event.offsetY
                        }));
                    }
                },
            },
            scales: {
                xAxes: [{
                    display: true,
                    scaleLabel: {
                        display: false
                    },
                    gridLines: {
                        color: 'rgb(0, 0, 0)',
                        lineWidth: 0.5,
                        drawBorder: true,
                        drawOnChartArea: false
                    },
                    ticks: {
                        maxTicksLimit: 4,
                        callback: (label: string): string[] => {
                            return label.split('#');
                        },
                        maxRotation: 0,
                        minRotation: 0,
                        fontSize: 11,
                        padding: 5
                    }
                }],
                yAxes: [{
                    id: 'y-axis-1',
                    display: true,
                    position: 'left',
                    scaleLabel: {
                        display: true,
                        labelString: 'Memory (bytes)',
                        fontSize: 14,
                        fontStyle: 'bold'
                    },
                    gridLines: {
                        color: 'rgb(0, 0, 0)',
                        lineWidth: 0.5,
                        drawBorder: true,
                        drawOnChartArea: false
                    },
                    ticks: {
                        beginAtZero: true,
                        maxTicksLimit: 5,
                        callback: (label: number): string => {
                            return this.convertWithUnit(label);
                        },
                        min: 0,
                        max: this.isDataEmpty(data) ? this.defaultYMax : undefined,
                        padding: 5
                    }
                },
                {
                    id: 'y-axis-2',
                    display: true,
                    position: 'right',
                    scaleLabel: {
                        display: true,
                        labelString: 'Full GC (ms)',
                        fontSize: 14,
                        fontStyle: 'bold'
                    },
                    gridLines: {
                        color: 'rgb(0, 0, 0)',
                        lineWidth: 0.5,
                        drawBorder: true,
                        drawOnChartArea: false
                    },
                    ticks: {
                        beginAtZero: true,
                        maxTicksLimit: 5,
                        min: 0,
                        padding: 5
                    }
                }]
            },
            legend: {
                display: true,
                labels: {
                    boxWidth: 30,
                    padding: 10
                }
            }
        };
    }

    private convertWithUnit(value: number): string {
        const unit = ['', 'K', 'M', 'G'];
        let result = value;
        let index = 0;
        while ( result >= 1000 ) {
            index++;
            result /= 1000;
        }

        result = Number.isInteger(result) ? result : Number(result.toFixed(2));
        return result + unit[index];
    }

    onShowHelp($event: MouseEvent): void {
        super.onShowHelp($event, HELP_VIEWER_LIST.AGENT_NON_HEAP);
    }
}
