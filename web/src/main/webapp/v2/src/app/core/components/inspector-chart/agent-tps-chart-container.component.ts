import { Component, OnInit, OnDestroy, ChangeDetectionStrategy, ChangeDetectorRef  } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import * as moment from 'moment-timezone';

import { Actions } from 'app/shared/store';
import { WebAppSettingDataService, NewUrlStateNotificationService, AnalyticsService, StoreHelperService, DynamicPopupService } from 'app/shared/services';
import { AgentTPSChartDataService } from './agent-tps-chart-data.service';
import { HELP_VIEWER_LIST } from 'app/core/components/help-viewer-popup/help-viewer-popup-container.component';
import { InspectorChartContainer } from 'app/core/components/inspector-chart/inspector-chart-container';
import { IChartDataFromServer } from 'app/core/components/inspector-chart/chart-data.service';

@Component({
    selector: 'pp-agent-tps-chart-container',
    templateUrl: './agent-tps-chart-container.component.html',
    styleUrls: ['./agent-tps-chart-container.component.css'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class AgentTPSChartContainerComponent extends InspectorChartContainer implements OnInit, OnDestroy {
    constructor(
        storeHelperService: StoreHelperService,
        changeDetector: ChangeDetectorRef,
        webAppSettingDataService: WebAppSettingDataService,
        newUrlStateNotificationService: NewUrlStateNotificationService,
        chartDataService: AgentTPSChartDataService,
        translateService: TranslateService,
        analyticsService: AnalyticsService,
        dynamicPopupService: DynamicPopupService
    ) {
        super(
            10,
            storeHelperService,
            changeDetector,
            webAppSettingDataService,
            newUrlStateNotificationService,
            chartDataService,
            translateService,
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
        return data < 0 ? null : Number(data.toFixed(2));
    }

    protected makeChartData(chartData: IChartDataFromServer): {[key: string]: any} {
        const xArr = [];
        const tpsSCArr = [];
        const tpsSNArr = [];
        const tpsUCArr = [];
        const tpsUNArr = [];
        const tpsTArr = [];

        const xData = chartData.charts.x;
        const tpsSC = chartData.charts.y['TPS_SAMPLED_CONTINUATION'];
        const tpsSN = chartData.charts.y['TPS_SAMPLED_NEW'];
        const tpsUC = chartData.charts.y['TPS_UNSAMPLED_CONTINUATION'];
        const tpsUN = chartData.charts.y['TPS_UNSAMPLED_NEW'];
        const tpsT = chartData.charts.y['TPS_TOTAL'];
        const dataCount = xData.length;

        for ( let i = 0 ; i < dataCount ; i++ ) {
            xArr.push(moment(xData[i]).tz(this.timezone).format(this.dateFormat[0]) + '#' + moment(xData[i]).tz(this.timezone).format(this.dateFormat[1]));
            if ( tpsSC.length === 0 ) {
                continue;
            }
            tpsSCArr.push(this.parseData(tpsSC[i][2]));
            tpsSNArr.push(this.parseData(tpsSN[i][2]));
            tpsUCArr.push(this.parseData(tpsUC[i][2]));
            tpsUNArr.push(this.parseData(tpsUN[i][2]));
            if ( tpsT ) {
                tpsTArr.push(this.parseData(tpsT[i][2]));
            } else {
                tpsTArr.push(this.parseData((tpsSC[i][2] + tpsSN[i][2] + tpsUC[i][2] + tpsUN[i][2])));
            }
        }
        return {
            x: xArr,
            tpsSC: tpsSCArr,
            tpsSN: tpsSNArr,
            tpsUC: tpsUCArr,
            tpsUN: tpsUNArr,
            tpsT: tpsTArr
        };
    }

    protected makeDataOption(data: {[key: string]: any}): {[key: string]: any} {
        return {
            labels: data.x,
            datasets: [{
                label: 'S.C',
                data: data.tpsSC,
                fill: true,
                borderWidth: 0.5,
                borderColor: 'rgb(214, 141, 8)',
                backgroundColor: 'rgba(214, 141, 8, 0.4)',
                pointRadius: 0,
                pointHoverRadius: 3
            }, {
                label: 'S.N',
                data: data.tpsSN,
                fill: true,
                borderWidth: 0.5,
                borderColor: 'rgb(252, 178, 65)',
                backgroundColor: 'rgba(252, 178, 65, 0.4)',
                pointRadius: 0,
                pointHoverRadius: 3
            }, {
                label: 'U.C',
                data: data.tpsUC,
                fill: true,
                borderWidth: 0.5,
                borderColor: 'rgb(90, 103, 166)',
                backgroundColor: 'rgba(90, 103, 166, 0.4)',
                pointRadius: 0,
                pointHoverRadius: 3
            }, {
                label: 'U.N',
                data: data.tpsUN,
                fill: true,
                borderWidth: 0.5,
                borderColor: 'rgb(160, 153, 255)',
                backgroundColor: 'rgba(160, 153, 255, 0.4)',
                pointRadius: 0,
                pointHoverRadius: 3
            }, {
                label: 'Total',
                data: data.tpsT,
                fill: false,
                borderWidth: 0.5,
                // borderColor: 'rgb(31, 119, 180)',
                // backgroundColor: 'rgba(31, 119, 180, 0.4)',
                borderColor: 'rgba(31, 119, 180, 0)',
                backgroundColor: 'rgba(255, 255, 255, 0)',
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
                text: 'TPS'
            },
            tooltips: {
                mode: 'index',
                intersect: false,
                callbacks: {
                    title: (value: {[key: string]: any}[]): string => {
                        return value[0].xLabel.join(' ');
                    },
                    label: (value: {[key: string]: any}, d: {[key: string]: any}): string => {
                        return `${d.datasets[value.datasetIndex].label}: ${isNaN(value.yLabel) ? `-` : value.yLabel.toFixed(1)}`;
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
                    stacked: true,
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
                    stacked: true,
                    display: true,
                    scaleLabel: {
                        display: true,
                        labelString: 'Transaction (count)',
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
                        callback: (value: number): number => {
                            const label =  Number.isInteger(value) ? value : Number(value.toFixed(1));
                            return label;
                        },
                        min: 0,
                        max: this.isDataEmpty(data) ? this.defaultYMax : undefined,
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
        super.onShowHelp($event, HELP_VIEWER_LIST.AGENT_TPS);
    }
}
