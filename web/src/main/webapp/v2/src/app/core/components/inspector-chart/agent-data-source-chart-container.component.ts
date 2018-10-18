import { Component, OnInit, OnDestroy, ViewChild, ChangeDetectionStrategy, ChangeDetectorRef  } from '@angular/core';
import { InspectorChartComponent } from './inspector-chart.component';
import { TranslateService } from '@ngx-translate/core';
import { filter, skip, tap } from 'rxjs/operators';
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
import { AgentDataSourceChartDataService, IAgentDataSourceChart } from './agent-data-source-chart-data.service';
import { HELP_VIEWER_LIST } from 'app/core/components/help-viewer-popup/help-viewer-popup-container.component';
import { InspectorChartContainer } from 'app/core/components/inspector-chart/inspector-chart-container';

@Component({
    selector: 'pp-agent-data-source-chart-container',
    templateUrl: './agent-data-source-chart-container.component.html',
    styleUrls: ['./agent-data-source-chart-container.component.css'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class AgentDataSourceChartContainerComponent extends InspectorChartContainer implements OnInit, OnDestroy {
    @ViewChild(InspectorChartComponent) inspectorChartComponent: InspectorChartComponent;
    private checkedSourceDataArr: {[key: string]: any}[];

    sourceDataArr: {[key: string]: any}[];
    infoTableObj: {[key: string]: any};

    constructor(
        storeHelperService: StoreHelperService,
        changeDetector: ChangeDetectorRef,
        webAppSettingDataService: WebAppSettingDataService,
        newUrlStateNotificationService: NewUrlStateNotificationService,
        chartDataService: AgentDataSourceChartDataService,
        translateService: TranslateService,
        ajaxExceptionCheckerService: AjaxExceptionCheckerService,
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
            ajaxExceptionCheckerService,
            analyticsService,
            dynamicPopupService
        );
    }

    ngOnInit() {
        this.initI18nText();
        this.initHoveredInfo();
        this.initTimezoneAndDateFormat();
        this.initInfoTableObj();
        this.initChartData();
    }

    ngOnDestroy() {
        this.unsubscribe.next();
        this.unsubscribe.complete();
    }

    private initInfoTableObj(): void {
        this.infoTableObj = {
            activeAvg: '-',
            activeMax: '-',
            totalMax: '-',
            id: '-',
            serviceType: '-',
            databaseName: '-',
            jdbcUrl: '-',
        };
    }

    protected initHoveredInfo(): void {
        this.hoveredInfo$ = this.storeHelperService.getHoverInfo(this.unsubscribe).pipe(
            skip(1),
            filter(() => {
                return !(!this.chartConfig || this.chartConfig.isDataEmpty);
            }),
            tap((hoverInfo: IHoveredInfo) => this.updateInfoTable(hoverInfo)),
        );
    }

    private updateInfoTable(hoverInfo: IHoveredInfo): void {
        if (hoverInfo.index !== -1) {
            const activeIndex = hoverInfo.index; // x축 기준 index
            const activeElements = this.inspectorChartComponent.getActiveTooltipElements(activeIndex);
            const eventCord = {x1: hoverInfo.offsetX, y1: hoverInfo.offsetY};
            const distanceArr = activeElements.map((element) => this.getDistanceBetweenPoints(eventCord, {x2: element._view.x, y2: element._view.y}));
            const minDistance = Math.min(...distanceArr);
            const elementIndex = distanceArr.indexOf(minDistance); // element들 중 event point에 가장 가까운 element의 index
            if (elementIndex !== -1) {
                this.infoTableObj = {
                    activeAvg: this.checkedSourceDataArr[elementIndex].activeAvg[activeIndex],
                    activeMax: this.checkedSourceDataArr[elementIndex].activeMax[activeIndex],
                    totalMax: this.checkedSourceDataArr[elementIndex].totalMax[activeIndex],
                    id: this.checkedSourceDataArr[elementIndex].id,
                    serviceType: this.checkedSourceDataArr[elementIndex].serviceType,
                    databaseName: this.checkedSourceDataArr[elementIndex].databaseName,
                    jdbcUrl: this.checkedSourceDataArr[elementIndex].jdbcUrl,
                };
            }
        }
    }

    private getDistanceBetweenPoints({x1, y1}: {x1: number, y1: number}, {x2, y2}: {x2: number, y2: number}): number {
        return Math.hypot(x1 - x2, y1 - y2);
    }

    protected getChartData(range: number[]): void {
        this.chartDataService.getData(range)
            .subscribe(
                (data: IAgentDataSourceChart[] | AjaxException) => {
                    if (this.ajaxExceptionCheckerService.isAjaxException(data)) {
                        this.setErrObj(data);
                    } else {
                        this.chartData = data;
                        this.sourceDataArr = this.makeChartData(data);
                        this.setChartConfig(this.sourceDataArr);
                    }
                },
                (err) => {
                    this.setErrObj();
                }
            );
    }

    onCheckedIdChange(checkedIdSet: Set<number>): void {
        this.setChartConfig(this.getCheckedSourceDataArr(checkedIdSet));
    }

    private getCheckedSourceDataArr(checkedIdSet: Set<number>): {[key: string]: any}[] {
        this.checkedSourceDataArr = this.sourceDataArr.filter((sourceData) => checkedIdSet.has(sourceData.id));
        return this.checkedSourceDataArr;
    }

    protected makeChartData(chartDataArr: IAgentDataSourceChart[]): {[key: string]: any}[] {
        return chartDataArr.map((chartData: IAgentDataSourceChart) => {
            return {
                x: chartData.charts.x.map((time: number) => moment(time).tz(this.timezone).format(this.dateFormat[0]) + '#' + moment(time).tz(this.timezone).format(this.dateFormat[1])),
                activeAvg: chartData.charts.y['ACTIVE_CONNECTION_SIZE'].map((arr: number[]) => this.parseData(arr[2])),
                activeMax: chartData.charts.y['ACTIVE_CONNECTION_SIZE'].map((arr: number[]) => this.parseData(arr[1])),
                totalMax: chartData.charts.y['MAX_CONNECTION_SIZE'].map((arr: number[]) => this.parseData(arr[1])),
                databaseName: chartData.databaseName,
                id: chartData.id,
                jdbcUrl: chartData.jdbcUrl,
                serviceType: chartData.serviceType,
            };
        });
    }

    protected makeDataOption(data: {[key: string]: any}[]): {[key: string]: any} {
        const colorMap = [
            '#850901', '#969755', '#421416', '#c8814b', '#aa8735', '#cd7af4', '#f6546a', '#1c1a1f', '#127999', '#b7ebd9',
            '#f6546a', '#bea87f', '#d1b4b0', '#e0d4ba', '#0795d9', '#43aa83', '#09d05b', '#c26e67', '#ed7575', '#96686a'
        ];
        const labels = this.sourceDataArr[0].x;

        return {
            labels,
            datasets: data.map((obj, i) => {
                return {
                    label: 'ActiveAvg',
                    data: obj.activeAvg,
                    fill: false,
                    borderWidth: 0.5,
                    borderColor: colorMap[i],
                    pointRadius: 0,
                    pointHoverRadius: 3
                };
            })
        };
    }

    protected makeNormalOption(data: {[key: string]: any}[]): {[key: string]: any} {
        return {
            responsive: true,
            title: {
                display: false,
                text: 'Data Source'
            },
            tooltips: {
                mode: 'index',
                intersect: false,
                callbacks: {
                    title: (value: {[key: string]: any}[]): string => {
                        return value[0].xLabel.join(' ');
                    },
                    label: (value: {[key: string]: any}, d: {[key: string]: any}): string => {
                        return '';
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
                        labelString: 'Connection (count)',
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
                        max: this.defaultYMax,
                        padding: 5
                    }
                }]
            },
            legend: {
                display: false,
                labels: {
                    boxWidth: 30,
                    padding: 10
                }
            }
        };
    }

    onShowHelp($event: MouseEvent): void {
        super.onShowHelp($event, HELP_VIEWER_LIST.AGENT_DATA_SOURCE);
    }
}
