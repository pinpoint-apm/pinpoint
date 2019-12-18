import { Component, OnInit, Input, ComponentFactoryResolver, Injector, OnDestroy } from '@angular/core';
import { Subject, forkJoin, combineLatest, of } from 'rxjs';
import { takeUntil, tap, switchMap, catchError } from 'rxjs/operators';
import * as moment from 'moment-timezone';
import { TranslateService } from '@ngx-translate/core';
import { PrimitiveArray, Data } from 'billboard.js';

import {
    StoreHelperService,
    AnalyticsService,
    DynamicPopupService,
    TRACKED_EVENT_LIST,
    MessageQueueService,
    MESSAGE_TO
} from 'app/shared/services';
import { InspectorPageService, ISourceForChart } from 'app/routes/inspector-page/inspector-page.service';
import { isThatType } from 'app/core/utils/util';
import { HELP_VIEWER_LIST, HelpViewerPopupContainerComponent } from 'app/core/components/help-viewer-popup/help-viewer-popup-container.component';
import { IInspectorChartData, InspectorChartDataService } from './inspector-chart-data.service';
import { IInspectorChartContainer } from './inspector-chart-container-factory';
import { ChartType, InspectorChartContainerFactory } from './inspector-chart-container-factory';

export enum Layer {
    LOADING = 'loading',
    RETRY = 'retry',
    CHART = 'chart'
}

@Component({
    selector: 'pp-inspector-chart-container',
    templateUrl: './inspector-chart-container.component.html',
    styleUrls: ['./inspector-chart-container.component.css']
})
export class InspectorChartContainerComponent implements OnInit, OnDestroy {
    @Input()
    set chartType(chartType: ChartType) {
        this._chartType = chartType;
        this.chartContainer = InspectorChartContainerFactory.createInspectorChartContainer(chartType, this.inspectorChartDataService);
    }

    get chartType(): ChartType {
        return this._chartType;
    }

    private _chartType: ChartType;
    private previousRange: number[];
    private timezone: string;
    private dateFormat: string[];
    private unsubscribe = new Subject<void>();

    chartContainer: IInspectorChartContainer;
    chartConfig: IChartConfig;
    isDataEmpty: boolean;
    showLoading: boolean;
    showRetry: boolean;
    dataFetchFailedText: string;
    dataEmptyText: string;
    retryMessage: string;
    chartVisibility = {};
    _activeLayer: Layer = Layer.LOADING;

    constructor(
        private storeHelperService: StoreHelperService,
        private inspectorChartDataService: InspectorChartDataService,
        private translateService: TranslateService,
        private analyticsService: AnalyticsService,
        private dynamicPopupService: DynamicPopupService,
        private componentFactoryResolver: ComponentFactoryResolver,
        private injector: Injector,
        private inspectorPageService: InspectorPageService,
        private messageQueueService: MessageQueueService,
    ) { }

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

    onMouseMove({clientY, target}: MouseEvent): void {
        const {height, top} = (target as SVGRectElement).getBoundingClientRect();
        const yRatio = (clientY - top) / height;

        this.messageQueueService.sendMessage({
            to: MESSAGE_TO.INSPECTOR_CHART_MOUSE_MOVE,
            param: yRatio
        });
    }

    private initChartData(): void {
        this.inspectorPageService.sourceForChart$.pipe(
            takeUntil(this.unsubscribe),
            tap(() => this.activeLayer = Layer.LOADING),
            tap(({range}: ISourceForChart) => this.previousRange = range),
            switchMap(({range}: ISourceForChart) => {
                return this.chartContainer.getData(range).pipe(
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
        this.chartContainer.getData(this.previousRange).pipe(
            catchError(() => of(null))
        ).subscribe((data) => this.chartDataResCallbackFn(data));
    }

    private chartDataResCallbackFn(data: IInspectorChartData | AjaxException | null): void {
        if (data === null || isThatType<AjaxException>(data, 'exception')) {
            this.activeLayer = Layer.RETRY;
            this.setRetryMessage(data);
        } else {
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

    private makeChartData(data: IInspectorChartData): PrimitiveArray[] {
        return this.chartContainer.makeChartData(data);
    }

    private makeDataOption(columns: PrimitiveArray[]): Data {
        return {
            x: 'x',
            columns,
            empty: {
                label: {
                    text: this.dataEmptyText
                }
            },
            ...this.chartContainer.makeDataOption()
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
                        right: 0
                    }
                },
                ...this.chartContainer.makeYAxisOptions(data)
            },
            point: {
                r: 0,
                focus: {
                    expand: {
                        r: 3
                    }
                }
            },
            resize: {
                auto: false
            },
            tooltip: {
                linked: true,
                format: {
                    value: (v: number, _: number, columnId: string, i: number) => {
                        return this.chartContainer.getTooltipFormat(v, columnId, i);
                    }
                },
                order: ''
            },
            transition: {
                duration: 0
            },
            zoom: {
                enabled: {
                    type: 'drag'
                }
            },
            ...this.chartContainer.makeElseOption()
        };
    }

    onShowHelp($event: MouseEvent): void {
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.TOGGLE_HELP_VIEWER, this.chartType);
        const {left, top, width, height} = ($event.target as HTMLElement).getBoundingClientRect();

        this.dynamicPopupService.openPopup({
            data: HELP_VIEWER_LIST[this.chartType as keyof typeof HELP_VIEWER_LIST],
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
