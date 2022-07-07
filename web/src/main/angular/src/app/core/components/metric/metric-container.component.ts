import { Component, Input, OnInit, OnDestroy, ComponentFactoryResolver, Injector } from '@angular/core';
import { filter, map, switchMap, takeUntil, tap, catchError } from 'rxjs/operators';
import { Data, line, PrimitiveArray, zoom } from 'billboard.js';
import * as moment from 'moment-timezone';

import { AnalyticsService, DynamicPopupService, NewUrlStateNotificationService, StoreHelperService } from 'app/shared/services';
import { UrlPathId } from 'app/shared/models';
import { MetricDataService } from './metric-data.service';
import { TranslateService } from '@ngx-translate/core';
import { combineLatest, forkJoin, of, Subject, EMPTY } from 'rxjs';
import { getMaxTickValue, makeXData } from 'app/core/utils/chart-util';
import { isEmpty } from 'app/core/utils/util';
import { getMetaInfo, Unit } from './metric-util';

export enum Layer {
    LOADING = 'loading',
    RETRY = 'retry',
    CHART = 'chart'
}

@Component({
    selector: 'pp-metric-container',
    templateUrl: './metric-container.component.html',
    styleUrls: ['./metric-container.component.css']
})
export class MetricContainerComponent implements OnInit, OnDestroy {
    @Input() type: string; // metricDefinitionId

    // private previousRange: number[];
    private previousParam: {[key: string]: any};
    private timezone: string;
    private dateFormat: string[];
    private unsubscribe = new Subject<void>();
    private defaultYMax = 100;

    private originalData: IMetricData;

    private metaInfo: {yMax: number, getFormat: Function};

    title: string;
    chartConfig: IChartConfig;
    isDataEmpty: boolean;
    showLoading: boolean;
    showRetry: boolean;
    dataFetchFailedText: string;
    dataEmptyText: string;
    retryMessage: string;
    chartVisibility = {};
    _activeLayer: Layer = Layer.LOADING;

    isGroupedMetric: boolean;
    selectedMetricGroupName: string;
    metricGroupList: string[];

    constructor(
        private storeHelperService: StoreHelperService,
        private newUrlStateNotificationService: NewUrlStateNotificationService,
        private metricDataService: MetricDataService,
        private translateService: TranslateService,
        private analyticsService: AnalyticsService,
        private dynamicPopupService: DynamicPopupService,
        private componentFactoryResolver: ComponentFactoryResolver,
        private injector: Injector,
    ) { }

    ngOnInit() {
        this.initI18nText();
        this.connectStore();
        this.newUrlStateNotificationService.onUrlStateChange$.pipe(
            takeUntil(this.unsubscribe),
            filter((urlService: NewUrlStateNotificationService) => {
                return !this.chartConfig || (urlService.isValueChanged(UrlPathId.PERIOD) || urlService.isValueChanged(UrlPathId.END_TIME));
            }),
            tap(() => this.activeLayer = Layer.LOADING),
            map((urlService: NewUrlStateNotificationService) => {
                const hostGroupName = urlService.getPathValue(UrlPathId.HOST_GROUP);
                const hostName = urlService.getPathValue(UrlPathId.HOST);
                const from = urlService.getStartTimeToNumber();
                const to = urlService.getEndTimeToNumber();

                return {hostGroupName, hostName, from, to, metricDefinitionId: this.type};
            }),
            tap((param: object) => this.previousParam = param),
            switchMap((param: object) => {
                return this.metricDataService.getMetricData(param).pipe(
                    catchError((error: IServerError) => {
                        this.activeLayer = Layer.RETRY;
                        this.setRetryMessage(error.message);
                        return EMPTY;
                    })
                );
            }),
            map((data: IMetricData) => {
                const {title, timestamp, metricValueGroups, unit} = data;

                this.title = title;
                this.isGroupedMetric = metricValueGroups.length > 1;
                this.metricGroupList = metricValueGroups.map(({groupName}: {groupName: string}) => groupName);
                this.originalData = data;
                this.selectedMetricGroupName = metricValueGroups[0].groupName;
                this.metaInfo = getMetaInfo(unit as Unit);

                return {timestamp, metricValues: metricValueGroups[0].metricValues};
            })
        ).subscribe((data: {timestamp: number[], metricValues: IMetricValue[]}) => {
            this.setChartConfig(this.makeChartData(data));
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

    private connectStore(): void {
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

    private setChartVisibility(showChart: boolean): void {
        this.chartVisibility = {
            'show-chart': showChart,
            'shady-chart': !showChart && this.chartConfig !== undefined,
        };
    }

    onChangeGroup(key: string): void {
        const data = {
            timestamp: this.originalData.timestamp,
            metricValues: this.originalData.metricValueGroups.find(({groupName}: {groupName: string}) => groupName === key).metricValues
        };

        this.setChartConfig(this.makeChartData(data));
        this.selectedMetricGroupName = key;
    }

    isActiveLayer(layer: string): boolean {
        return this.activeLayer === layer;
    }

    onRetry(): void {
        this.activeLayer = Layer.LOADING;
        this.metricDataService.getMetricData(this.previousParam).pipe(
            catchError((error: IServerError) => {
                this.activeLayer = Layer.RETRY;
                this.setRetryMessage(error.message);
                return EMPTY;
            }),
        ).subscribe((data: IMetricData) => {
            this.setChartConfig(this.makeChartData(data));
        });
    }

    private setChartConfig(data: PrimitiveArray[]): void {
        this.chartConfig =  {
            dataConfig: this.makeDataOption(data),
            elseConfig: this.makeElseOption(data),
        };
        this.isDataEmpty = this.isEmpty(data);
    }

    private setRetryMessage(message: string): void {
        this.retryMessage = message;
    }

    private isEmpty(data: PrimitiveArray[]): boolean {
        return data.length === 0 || data.slice(1).every((d: PrimitiveArray) => d.length === 1);
    }

    private makeChartData({timestamp, metricValues}: {[key: string]: any}): PrimitiveArray[] {
        return [
            ['x', ...makeXData(timestamp)],
            ...metricValues.map(({fieldName, values}: IMetricValue) => {
                return [fieldName, ...values.map((v: number) => v < 0 ? null : v)];
            })
        ];
    }

    // TODO: Apply Tags in something in chart-config
    private makeDataOption(columns: PrimitiveArray[]): Data {
        return {
            x: 'x',
            columns,
            empty: {
                label: {
                    text: this.dataEmptyText
                }
            },
            type: line(),
            // names: {},
            // colors: {}
        };
    }

    private makeElseOption(data: PrimitiveArray[]): {[key: string]: any} {
        return {
            padding: {
                top: 20,
                bottom: 15,
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
                        // right: 0
                    }
                },
                y: {
                    label: {
                        // text: 'File Descriptor (count)',
                        // position: 'outer-middle'
                    },
                    tick: {
                        // count: 5,
                        format: this.metaInfo.getFormat
                    },
                    padding: {
                        // top: 0,
                        bottom: 0
                    },
                    min: 0,
                    // max: (() => {
                    //     if (this.metaInfo.yMax) {
                    //         return this.metaInfo.yMax;
                    //     }

                    //     // TODO: Use values in billboardjs?
                    //     const maxTickValue = getMaxTickValue(data, 1);

                    //     return maxTickValue === 0 ? this.defaultYMax : maxTickValue;
                    // })(),
                    default: [0, this.defaultYMax]
                }
            },
            point: {
                r: 0,
                focus: {
                    only: true,
                    expand: {
                        r: 3
                    }
                }
            },
            resize: {
                auto: false
                // auto: true
            },
            tooltip: {
                linked: true,
                format: {
                    value: this.metaInfo.getFormat
                },
                order: ''
            },
            transition: {
                duration: 0
            },
            zoom: {
                enabled: zoom(),
                type: 'drag'
            },
            legend: {
                // position: 'right'
            }
        };
    }

    onShowHelp($event: MouseEvent): void {
        // const {left, top, width, height} = ($event.target as HTMLElement).getBoundingClientRect();

        // this.dynamicPopupService.openPopup({
        //     data: HELP_VIEWER_LIST[this.chartType as keyof typeof HELP_VIEWER_LIST],
        //     coord: {
        //         coordX: left + width / 2,
        //         coordY: top + height / 2
        //     },
        //     component: HelpViewerPopupContainerComponent
        // }, {
        //     resolver: this.componentFactoryResolver,
        //     injector: this.injector
        // });
    }
}
