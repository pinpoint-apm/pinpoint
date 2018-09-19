import { Component, OnInit, OnDestroy, ChangeDetectionStrategy, ChangeDetectorRef  } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import * as moment from 'moment-timezone';

import {
    WebAppSettingDataService,
    NewUrlStateNotificationService,
    AjaxExceptionCheckerService,
    AnalyticsService,
    StoreHelperService,
    DynamicPopupService
} from 'app/shared/services';
import { Actions } from 'app/shared/store';
import { AgentCPUChartDataService } from './agent-cpu-chart-data.service';
import { HELP_VIEWER_LIST } from 'app/core/components/help-viewer-popup/help-viewer-popup-container.component';
import { InspectorChartContainer } from 'app/core/components/inspector-chart/inspector-chart-container';
import { IChartDataFromServer } from 'app/core/components/inspector-chart/chart-data.service';

@Component({
    selector: 'pp-agent-cpu-chart-container',
    templateUrl: './agent-cpu-chart-container.component.html',
    styleUrls: ['./agent-cpu-chart-container.component.css'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class AgentCPUChartContainerComponent extends InspectorChartContainer implements OnInit, OnDestroy {
    constructor(
        storeHelperService: StoreHelperService,
        changeDetector: ChangeDetectorRef,
        webAppSettingDataService: WebAppSettingDataService,
        newUrlStateNotificationService: NewUrlStateNotificationService,
        chartDataService: AgentCPUChartDataService,
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

    protected parseData(data: number): number | null {
        return data === -1 ? null : Number(data.toFixed(2));
    }

    protected makeChartData(chartData: IChartDataFromServer): {[key: string]: any} {
        const xArr = [];
        const jvmArr = [];
        const systemArr = [];
        const maxArr = [];

        const xData = chartData.charts.x;
        const cpuJVM = chartData.charts.y['CPU_LOAD_JVM'];
        const cpuSystem = chartData.charts.y['CPU_LOAD_SYSTEM'];
        const dataCount = xData.length;

        for ( let i = 0 ; i < dataCount ; i++ ) {
            xArr.push(moment(xData[i]).tz(this.timezone).format(this.dateFormat[0]) + '#' + moment(xData[i]).tz(this.timezone).format(this.dateFormat[1]));
            maxArr.push(100);
            if ( cpuJVM.length === 0 ) {
                continue;
            }
            jvmArr.push(this.parseData(cpuJVM[i][1]));
            systemArr.push(this.parseData(cpuSystem[i][1]));
        }
        return {
            x: xArr,
            jvm: jvmArr,
            system: systemArr
        };
    }

    protected makeDataOption(data: {[key: string]: any}): {[key: string]: any} {
        return {
            labels: data.x,
            datasets: [{
                label: 'JVM',
                data: data.jvm,
                fill: false,
                borderWidth: 0.5,
                borderColor: 'rgb(31, 119, 180)',
                backgroundColor: 'rgba(31, 119, 180, 0.4)',
                pointRadius: 0,
                pointHoverRadius: 3
            }, {
                label: 'System',
                data: data.system,
                fill: true,
                borderWidth: 0.5,
                borderColor: 'rgb(174, 199, 232)',
                backgroundColor: 'rgba(174, 199, 232, 0.4)',
                pointRadius: 0,
                pointHoverRadius: 3
            }]
        };
    }

    protected makeNormalOption(data: {[key: string]: any}): {[key: string]: any} {
        return {
            responsive: true,
            title: {
                display: false,
                text: 'JVM/System CPU Usage'
            },
            tooltips: {
                mode: 'index',
                intersect: false,
                callbacks: {
                    title: (value: {[key: string]: any}[]): string => {
                        return value[0].xLabel.join(' ');
                    },
                    label: (value: {[key: string]: any}, d: {[key: string]: any}): string => {
                        return `${d.datasets[value.datasetIndex].label}: ${isNaN(value.yLabel) ? `-` : value.yLabel}%`;
                    }
                }
            },
            hover: {
                mode: 'index',
                intersect: false,
                onHover: (event: MouseEvent, elements: {[key: string]: any}[]): void => {
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
                    display: true,
                    scaleLabel: {
                        display: true,
                        labelString: 'CPU Usage (%)',
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
                            return `${label}%`;
                        },
                        min: 0,
                        max: this.defaultYMax,
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

    onShowHelp($event: MouseEvent): void {
        super.onShowHelp($event, HELP_VIEWER_LIST.AGENT_CPU_USAGE);
    }
}
