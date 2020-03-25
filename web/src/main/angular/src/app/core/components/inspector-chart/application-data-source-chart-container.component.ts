import { Component, OnInit, OnDestroy, ComponentFactoryResolver, Injector } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import * as moment from 'moment-timezone';
import { PrimitiveArray, Data } from 'billboard.js';
import { Subject, forkJoin, combineLatest, of } from 'rxjs';
import { takeUntil, tap, switchMap, catchError } from 'rxjs/operators';

import { AnalyticsService, StoreHelperService, DynamicPopupService, TRACKED_EVENT_LIST } from 'app/shared/services';
import { ApplicationDataSourceChartDataService, IApplicationDataSourceChart } from './application-data-source-chart-data.service';
import { HELP_VIEWER_LIST, HelpViewerPopupContainerComponent } from 'app/core/components/help-viewer-popup/help-viewer-popup-container.component';
import { isThatType } from 'app/core/utils/util';
import { InspectorPageService, ISourceForChart } from 'app/routes/inspector-page/inspector-page.service';
import { IInspectorChartData } from './inspector-chart-data.service';
import { makeXData, makeYData, getMaxTickValue } from 'app/core/utils/chart-util';
import { Layer } from './inspector-chart-container.component';

@Component({
    selector: 'pp-application-data-source-chart-container',
    templateUrl: './application-data-source-chart-container.component.html',
    styleUrls: ['./application-data-source-chart-container.component.css'],
})
export class ApplicationDataSourceChartContainerComponent implements OnInit, OnDestroy {
    private minAgentIdList: string[];
    private maxAgentIdList: string[];
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
    sourceForList: {serviceType: string, jdbcUrl: string}[];

    constructor(
        private storeHelperService: StoreHelperService,
        private inspectorChartDataService: ApplicationDataSourceChartDataService,
        private translateService: TranslateService,
        private analyticsService: AnalyticsService,
        private dynamicPopupService: DynamicPopupService,
        private componentFactoryResolver: ComponentFactoryResolver,
        private injector: Injector,
        private inspectorPageService: InspectorPageService,
    ) {}

    ngOnInit() {
        this.initI18nText();
        this.initTimezoneAndDateFormat();
        this.initChartData();
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

    onSourceDataSelected(index: number): void {
        this.activeLayer = Layer.LOADING;
        this.setMinMaxAgentIdList((this.chartData as IApplicationDataSourceChart[])[index]);
        this.setChartConfig(this.makeChartData((this.chartData as IApplicationDataSourceChart[])[index]));
    }

    chartDataResCallbackFn(data: IApplicationDataSourceChart[] | AjaxException | null): void {
        if (data === null || isThatType<AjaxException>(data, 'exception')) {
            this.activeLayer = Layer.RETRY;
            this.setRetryMessage(data);
        } else {
            this.chartData = data;
            this.sourceForList = data.map(({serviceType, jdbcUrl}: IApplicationDataSourceChart) => ({serviceType, jdbcUrl}));
            this.setMinMaxAgentIdList(data[0]);
            this.setChartConfig(this.makeChartData(data[0]));
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

    private setMinMaxAgentIdList(data: IApplicationDataSourceChart): void {
        this.minAgentIdList = data.charts.y['ACTIVE_CONNECTION_SIZE'].map((arr: any[]) => arr[1]);
        this.maxAgentIdList = data.charts.y['ACTIVE_CONNECTION_SIZE'].map((arr: any[]) => arr[3]);
    }

    private makeChartData({charts}: IApplicationDataSourceChart): PrimitiveArray[] {
        return [
            ['x', ...makeXData(charts.x)],
            ['max', ...makeYData(charts.y['ACTIVE_CONNECTION_SIZE'], 2)],
            ['avg', ...makeYData(charts.y['ACTIVE_CONNECTION_SIZE'], 4)],
            ['min', ...makeYData(charts.y['ACTIVE_CONNECTION_SIZE'], 0)],
        ];
    }

    private makeDataOption(columns: PrimitiveArray[]): Data {
        return {
            x: 'x',
            columns,
            type: 'spline',
            names: {
                min: 'Min',
                avg: 'Avg',
                max: 'Max',
            },
            colors: {
                min: '#66B2FF',
                avg: '#4C0099',
                max: '#0000CC',
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
            line: {
                classes: ['min', 'avg', 'max']
            },
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
                format: {
                    value: (v: number, _: number, columnId: string, index: number) => v < 0  ? '-' : `${v} ${this.getAgentId(columnId, index)}`
                }
            },
            transition: {
                duration: 0
            },
            zoom: {
                enabled: {
                    type: 'drag'
                }
            }
        };
    }

    private getAgentId(columnId: string, index: number): string {
        return columnId === 'avg' ? '' : `(${columnId === 'min' ? this.minAgentIdList[index] : this.maxAgentIdList[index]})`;
    }

    onShowHelp($event: MouseEvent): void {
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.TOGGLE_HELP_VIEWER, HELP_VIEWER_LIST.APPLICATION_DATA_SOURCE);
        const {left, top, width, height} = ($event.target as HTMLElement).getBoundingClientRect();

        this.dynamicPopupService.openPopup({
            data: HELP_VIEWER_LIST.APPLICATION_DATA_SOURCE,
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
