import { Component, OnInit, OnDestroy, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import * as moment from 'moment-timezone';

import { Actions } from 'app/shared/store';
import { WebAppSettingDataService, NewUrlStateNotificationService, AjaxExceptionCheckerService, AnalyticsService, StoreHelperService, DynamicPopupService } from 'app/shared/services';
import { ApplicationOpenFileDescriptorChartDataService } from './application-open-file-descriptor-chart-data.service';
import { HELP_VIEWER_LIST } from 'app/core/components/help-viewer-popup/help-viewer-popup-container.component';
import { InspectorChartContainer } from 'app/core/components/inspector-chart/inspector-chart-container';
import { IChartDataFromServer } from 'app/core/components/inspector-chart/chart-data.service';

@Component({
    selector: 'pp-application-open-file-descriptor-chart-container',
    templateUrl: './application-open-file-descriptor-chart-container.component.html',
    styleUrls: ['./application-open-file-descriptor-chart-container.component.css'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class ApplicationOpenFileDescriptorChartContainerComponent extends InspectorChartContainer implements OnInit, OnDestroy {
    constructor(
        storeHelperService: StoreHelperService,
        changeDetector: ChangeDetectorRef,
        webAppSettingDataService: WebAppSettingDataService,
        newUrlStateNotificationService: NewUrlStateNotificationService,
        chartDataService: ApplicationOpenFileDescriptorChartDataService,
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

    protected makeChartData(data: IChartDataFromServer): {[key: string]: any} {
        return {
            x: data.charts.x.map((time: number) => moment(time).tz(this.timezone).format(this.dateFormat[0]) + '#' + moment(time).tz(this.timezone).format(this.dateFormat[1])),
            minArr: data.charts.y['OPEN_FILE_DESCRIPTOR_COUNT'].map((arr: any[]) => this.parseData(arr[0])),
            minAgentIdArr: data.charts.y['OPEN_FILE_DESCRIPTOR_COUNT'].map((arr: any[]) => arr[1]),
            maxArr: data.charts.y['OPEN_FILE_DESCRIPTOR_COUNT'].map((arr: any[]) => this.parseData(arr[2])),
            maxAgentIdArr: data.charts.y['OPEN_FILE_DESCRIPTOR_COUNT'].map((arr: any[]) => arr[3]),
            avgArr: data.charts.y['OPEN_FILE_DESCRIPTOR_COUNT'].map((arr: any[]) => this.parseData(arr[4])),
        };
    }

    protected makeDataOption(data: {[key: string]: any}): {[key: string]: any} {
        return {
            labels: data.x,
            datasets: [{
                label: 'Min',
                data: data.minArr,
                fill: false,
                borderDash: [2, 2],
                borderWidth: 1.5,
                borderColor: '#66B2FF',
                backgroundColor: '#66B2FF',
                pointRadius: 0,
                pointHoverRadius: 3
            }, {
                label: 'Avg',
                data: data.avgArr,
                fill: false,
                borderWidth: 1.5,
                borderColor: '#4C0099',
                backgroundColor: '#4C0099',
                pointRadius: 0,
                pointHoverRadius: 3
            }, {
                label: 'Max',
                data: data.maxArr,
                fill: false,
                borderDash: [2, 2],
                borderWidth: 1.5,
                borderColor: '#0000CC',
                backgroundColor: '#0000CC',
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
                text: 'Open File Descriptor'
            },
            tooltips: {
                mode: 'index',
                intersect: false,
                callbacks: {
                    title: (value: {[key: string]: any}[]): string => {
                        return value[0].xLabel.join(' ');
                    },
                    label: (value: {[key: string]: any}, d: {[key: string]: any}): string => {
                        const label = d.datasets[value.datasetIndex].label;
                        const index = value.index;

                        return `${label}: ${isNaN(value.yLabel) ? `-` : value.yLabel + ` ` + this.getAgentId(data.minAgentIdArr, data.maxAgentIdArr, label, index)}`;
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
                        labelString: 'File Descriptor (count)',
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
                        max: this.isDataEmpty(data) ? this.defaultYMax : undefined,
                        padding: 5
                    }
                }]
            },
            legend: {
                display: true,
                labels: {
                    boxWidth: 50,
                    padding: 10
                }
            }
        };
    }

    private getAgentId(minAgentIdArr: string[], maxAgentIdArr: string[], label: string, index: number): string {
        return label === 'Avg' ? '' : `(${label === 'Min' ? minAgentIdArr[index] : maxAgentIdArr[index]})`;
    }

    onShowHelp($event: MouseEvent): void {
        super.onShowHelp($event, HELP_VIEWER_LIST.APPLICATION_OPEN_FILE_DESCRIPTOR);
    }
}
