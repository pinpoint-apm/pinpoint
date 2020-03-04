import { Component, OnInit, Input, OnDestroy, ViewChild } from '@angular/core';
import { Subject, forkJoin, combineLatest, of } from 'rxjs';
import { TranslateService } from '@ngx-translate/core';
import { PrimitiveArray, Data } from 'billboard.js';
import * as moment from 'moment-timezone';

import { ChartType, InspectorChartContainerFactory, IInspectorChartContainer } from './inspector-chart-container-factory';
import { InspectorChartComponent } from './inspector-chart.component';
import { StoreHelperService, NewUrlStateNotificationService } from 'app/shared/services';
import { InspectorChartDataService, IInspectorChartData } from './inspector-chart-data.service';
import { UrlPathId } from 'app/shared/models';
import { catchError } from 'rxjs/operators';
import { isThatType } from 'app/core/utils/util';

export enum Layer {
    LOADING = 'loading',
    RETRY = 'retry',
    CHART = 'chart'
}

@Component({
    selector: 'pp-transaction-view-chart-container',
    templateUrl: './transaction-view-chart-container.component.html',
    styleUrls: ['./transaction-view-chart-container.component.css']
})
export class TransactionViewChartContainerComponent implements OnInit, OnDestroy {
    @ViewChild(InspectorChartComponent, { static: true }) component: InspectorChartComponent;
    @Input()
    set chartType(chartType: ChartType) {
        this._chartType = chartType;
        this.chartContainer = InspectorChartContainerFactory.createInspectorChartContainer(chartType, this.inspectorChartDataService);
    }

    get chartType(): ChartType {
        return this._chartType;
    }

    private _chartType: ChartType;
    private timezone: string;
    private dateFormat: string[];
    private unsubscribe = new Subject<void>();

    chartContainer: IInspectorChartContainer;
    chartConfig: IChartConfig;
    showLoading: boolean;
    showRetry: boolean;
    dataFetchFailedText: string;
    dataEmptyText: string;
    retryMessage: string;
    chartVisibility = {};
    _activeLayer: Layer = Layer.LOADING;

    constructor(
        private storeHelperService: StoreHelperService,
        private newUrlStateNotificationService: NewUrlStateNotificationService,
        private inspectorChartDataService: InspectorChartDataService,
        private translateService: TranslateService
    ) {}

    ngOnInit() {
        this.initI18nText();
        this.initTimezoneAndDateFormat();
        this.getChartData(this.getTimeRange());
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

    onBackToTheView(): void {
        this.component.resize();
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

    private getTimeRange(): number[] {
        const focusTime = Number(this.newUrlStateNotificationService.getPathValue(UrlPathId.FOCUS_TIMESTAMP));
        const range = 600000;

        return [focusTime - range, focusTime + range];
    }

    onRetry(): void {
        this.activeLayer = Layer.LOADING;
        this.getChartData(this.getTimeRange());
    }

    private getChartData(range: number[]): void {
        this.chartContainer.getData(range).pipe(
            catchError(() => of(null))
        ).subscribe((data: IInspectorChartData | AjaxException | null) => {
            if (data === null || isThatType<AjaxException>(data, 'exception')) {
                this.activeLayer = Layer.RETRY;
                this.setRetryMessage(data);
            } else {
                this.setChartConfig(this.makeChartData(data));
            }
        });
    }

    private setChartConfig(data: PrimitiveArray[]): void {
        this.chartConfig =  {
            dataConfig: this.makeDataOption(data),
            elseConfig: this.makeElseOption(data),
        };
    }

    private setRetryMessage(data: any): void {
        this.retryMessage = data ? data.exception.message : this.dataFetchFailedText;
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
                top: 10,
                bottom: 15,
                right: 45,
            },
            axis: {
                x: {
                    type: 'timeseries',
                    tick: {
                        count: 7,
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
                }
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
}
