import { Component, OnInit, OnDestroy, ComponentFactoryResolver, Injector } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { PrimitiveArray, Data, DataItem } from 'billboard.js';
import { Subject, forkJoin, combineLatest, of } from 'rxjs';
import { tap, takeUntil, switchMap, catchError } from 'rxjs/operators';
import * as moment from 'moment-timezone';

import {
    AnalyticsService,
    StoreHelperService,
    DynamicPopupService,
    MessageQueueService,
    MESSAGE_TO,
    TRACKED_EVENT_LIST
} from 'app/shared/services';
import { AgentDataSourceChartDataService, IAgentDataSourceChart } from './agent-data-source-chart-data.service';
import { HELP_VIEWER_LIST, HelpViewerPopupContainerComponent } from 'app/core/components/help-viewer-popup/help-viewer-popup-container.component';
import { isThatType } from 'app/core/utils/util';
import { InspectorPageService, ISourceForChart } from 'app/routes/inspector-page/inspector-page.service';
import { IInspectorChartData } from './inspector-chart-data.service';
import { Layer } from './inspector-chart-container.component';
import { makeXData, makeYData, getMaxTickValue } from 'app/core/utils/chart-util';

@Component({
    selector: 'pp-agent-data-source-chart-container',
    templateUrl: './agent-data-source-chart-container.component.html',
    styleUrls: ['./agent-data-source-chart-container.component.css'],
})
export class AgentDataSourceChartContainerComponent implements OnInit, OnDestroy {
    private onoverDataHolder: DataItem[] = [];
    private yRatio: number;
    private previousRange: number[];
    private chartData: IInspectorChartData[];
    private timezone: string;
    private dateFormat: string[];
    private unsubscribe = new Subject<void>();
    private defaultYMax = 4;

    chartConfig: IChartConfig;
    isDataEmpty: boolean;
    showLoading: boolean;
    showRetry: boolean;
    dataFetchFailedText: string;
    dataEmptyText: string;
    retryMessage: string;
    chartVisibility = {};
    _activeLayer: Layer = Layer.LOADING;
    sourceDataArr: {[key: string]: any}[];
    sourceListForFilter: {databaseName: string, id: number}[];
    infoTableObj: {[key: string]: any};

    constructor(
        private storeHelperService: StoreHelperService,
        private inspectorChartDataService: AgentDataSourceChartDataService,
        private translateService: TranslateService,
        private analyticsService: AnalyticsService,
        private dynamicPopupService: DynamicPopupService,
        private componentFactoryResolver: ComponentFactoryResolver,
        private injector: Injector,
        private inspectorPageService: InspectorPageService,
        private messageQueueService: MessageQueueService,
    ) {}

    ngOnInit() {
        this.initI18nText();
        this.initTimezoneAndDateFormat();
        this.initInfoTableObj();
        this.initChartData();
        this.messageQueueService.receiveMessage(this.unsubscribe, MESSAGE_TO.INSPECTOR_CHART_MOUSE_MOVE).subscribe((yRatio: number) => {
            this.yRatio = yRatio;
        });
    }

    ngOnDestroy() {
        this.unsubscribe.next();
        this.unsubscribe.complete();
    }

    private initI18nText(): void {
        forkJoin(
            this.translateService.get('COMMON.FAILED_TO_FETCH_DATA'),
            this.translateService.get('COMMON.NO_DATA'),
        ).subscribe(([dataFetchFailedText, dataEmptyText]: string[]) => {
           this.dataFetchFailedText = dataFetchFailedText;
           this.dataEmptyText = dataEmptyText;
        });
    }

    private initTimezoneAndDateFormat(): void {
        combineLatest(
            this.storeHelperService.getTimezone(this.unsubscribe),
            this.storeHelperService.getDateFormatArray(this.unsubscribe, 4, 5)
        ).subscribe(([timezone, dateFormat]: [string, string[]]) => {
            this.timezone = timezone;
            this.dateFormat = dateFormat;
        });
    }

    set activeLayer(layer: Layer) {
        this._activeLayer = layer;
        this.setChartVisibility(this._activeLayer === Layer.CHART);
    }

    get activeLayer(): Layer {
        return this._activeLayer;
    }

    onRendered(): void {
        this.activeLayer = Layer.CHART;
    }

    onMouseMove({clientY, target}: MouseEvent): void {
        const {height, top} = (target as SVGRectElement).getBoundingClientRect();
        const yRatio = (clientY - top) / height;

        this.messageQueueService.sendMessage({
            to: MESSAGE_TO.INSPECTOR_CHART_MOUSE_MOVE,
            param: yRatio
        });
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

    private updateInfoTable({id, index}: DataItem): void {
        this.infoTableObj = (this.chartData as IAgentDataSourceChart[])
            .filter(({id: sourceId}: IAgentDataSourceChart) => sourceId === Number(id))
            .reduce((acc: any, data: IAgentDataSourceChart) => {
                const {id: sourceId, jdbcUrl, serviceType, databaseName, charts} = data;

                return {
                    id: sourceId,
                    jdbcUrl,
                    serviceType,
                    databaseName,
                    activeAvg: charts.y['ACTIVE_CONNECTION_SIZE'].map((d: number[]) => d[2])[index],
                    activeMax: charts.y['ACTIVE_CONNECTION_SIZE'].map((d: number[]) => d[1])[index],
                    totalMax: charts.y['MAX_CONNECTION_SIZE'].map((d: number[]) => d[1])[index]
                };
            }, {});
    }

    private initChartData(): void {
        this.inspectorPageService.sourceForChart$.pipe(
            takeUntil(this.unsubscribe),
            tap(() => this.activeLayer = Layer.LOADING),
            tap(({range}: ISourceForChart) => this.previousRange = range),
            switchMap(({range}: ISourceForChart) => {
                return this.inspectorChartDataService.getData(range).pipe(
                    catchError(() => of(null))
                );
            })
        ).subscribe((data) => this.chartDataResCallbackFn(data));
    }

    private setChartVisibility(showChart: boolean): void {
        this.chartVisibility = {
            'show-chart': showChart,
            'shady-chart': !showChart && this.chartConfig !== undefined,
        };
    }

    isActiveLayer(layer: string): boolean {
        return this.activeLayer === layer;
    }

    onRetry(): void {
        this.activeLayer = Layer.LOADING;
        this.inspectorChartDataService.getData(this.previousRange).pipe(
            catchError(() => of(null))
        ).subscribe((data) => this.chartDataResCallbackFn(data));
    }

    private chartDataResCallbackFn(data: IAgentDataSourceChart[] | AjaxException | null): void {
        if (!data || isThatType<AjaxException>(data, 'exception')) {
            this.activeLayer = Layer.RETRY;
            this.setRetryMessage(data);
        } else {
            this.chartData = data;
            this.sourceListForFilter = data.map(({databaseName, id}: IAgentDataSourceChart) => ({databaseName, id}));
            this.setChartConfig(this.makeChartData(data));
        }
    }

    private setChartConfig(data: PrimitiveArray[]): void {
        this.chartConfig =  {
            dataConfig: this.makeDataOption(data),
            elseConfig: this.makeElseOption(data),
        };
        this.isDataEmpty = this.isEmpty(data);
    }

    private setRetryMessage(data: any): void {
        this.retryMessage = data ? data.exception.message : this.dataFetchFailedText;
    }

    private isEmpty(data: PrimitiveArray[]): boolean {
        return data.length === 0 || data.slice(1).every((d: PrimitiveArray) => d.length === 1);
    }

    onCheckedIdChange(checkedIdSet: Set<number>): void {
        this.activeLayer = Layer.LOADING;
        this.setChartConfig(this.makeChartData((this.chartData as IAgentDataSourceChart[]).filter(({id}: IAgentDataSourceChart) => checkedIdSet.has(id))));
    }

    private makeChartData(data: IAgentDataSourceChart[]): PrimitiveArray[] {
        return data.length === 0
            ? []
            : [
                ['x', ...makeXData(data[0].charts.x)],
                ...data.map((d: IAgentDataSourceChart) => {
                    return [d.id.toString(), ...makeYData(d.charts.y['ACTIVE_CONNECTION_SIZE'], 2)];
                })
            ];
    }

    private makeDataOption(columns: PrimitiveArray[]): Data {
        const colorList = [
            '#850901', '#969755', '#421416', '#c8814b', '#aa8735', '#cd7af4', '#f6546a', '#1c1a1f', '#127999', '#b7ebd9',
            '#f6546a', '#bea87f', '#d1b4b0', '#e0d4ba', '#0795d9', '#43aa83', '#09d05b', '#c26e67', '#ed7575', '#96686a'
        ];
        const dataLength = columns.slice(1).length;
        const self = this;

        return {
            x: 'x',
            columns,
            type: 'spline',
            colors: columns.slice(1).reduce((acc: {[key: string]: string}, [key]: PrimitiveArray, i: number) => {
                return { ...acc, [key as string]: colorList[i]};
            }, {}),
            onover: function(d: DataItem) {
                self.onoverDataHolder.push(d);
                const max = this.axis.max().y;

                if (self.onoverDataHolder.length === dataLength && self.yRatio) {
                    const relativeYValueInChart = max - self.yRatio * max; // 차트안에서 mouse y위치의 상대적 value.
                    const dist = self.onoverDataHolder.map(({value}: DataItem) => Math.abs(value - relativeYValueInChart));
                    const index = dist.indexOf(Math.min(...dist));

                    self.updateInfoTable(self.onoverDataHolder[index]);
                    self.onoverDataHolder = [];
                }
            },
            empty: {
                label: {
                    text: this.dataEmptyText
                }
            }
        };
    }

    private makeElseOption(data: PrimitiveArray[]): {[key: string]: any} {
        return {
            padding: {
                top: 20,
                bottom: 35,
                right: 45,
            },
            axis: {
                x: {
                    type: 'timeseries',
                    tick: {
                        count: 4,
                        format: (time: Date) => {
                            return moment(time).tz(this.timezone).format(this.dateFormat[0]) + '\n' + moment(time).tz(this.timezone).format(this.dateFormat[1]);
                        }
                    },
                    padding: {
                        left: 0,
                        right: 0
                    }
                },
                y: {
                    label: {
                        text: 'Connection (count)',
                        position: 'outer-middle'
                    },
                    tick: {
                        count: 5,
                    },
                    padding: {
                        top: 0,
                        bottom: 0
                    },
                    min: 0,
                    max: (() => {
                        const maxTickValue = getMaxTickValue(data, 1);

                        return maxTickValue === 0 ? this.defaultYMax : maxTickValue;
                    })(),
                    default: [0, this.defaultYMax]
                },
            },
            point: {
                r: 0,
                focus: {
                    expand: {
                        r: 3
                    }
                }
            },
            tooltip: {
                linked: true,
                contents: function(d: any, defaultTitleFormat: Function) {
                    const title = defaultTitleFormat(d[0].x);

                    return `<table class="bb-tooltip"><th>${title}</th></table>`;
                },
            },
            transition: {
                duration: 0
            },
            legend: {
                show: false
            },
            zoom: {
                enabled: {
                    type: 'drag'
                }
            }
        };
    }

    onShowHelp($event: MouseEvent): void {
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.TOGGLE_HELP_VIEWER, HELP_VIEWER_LIST.AGENT_DATA_SOURCE);
        const {left, top, width, height} = ($event.target as HTMLElement).getBoundingClientRect();

        this.dynamicPopupService.openPopup({
            data: HELP_VIEWER_LIST.AGENT_DATA_SOURCE,
            coord: {
                coordX: left + width / 2,
                coordY: top + height / 2
            },
            component: HelpViewerPopupContainerComponent
        }, {
            resolver: this.componentFactoryResolver,
            injector: this.injector
        });
    }
}
