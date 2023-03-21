import { ChangeDetectorRef, Component, ElementRef, OnDestroy, OnInit } from '@angular/core';
import { combineLatest, EMPTY, forkJoin, iif, Observable, of, Subject } from 'rxjs';
import { catchError, filter, map, startWith, switchMap, takeUntil, tap } from 'rxjs/operators';
import { Data, PrimitiveArray, bar, zoom } from 'billboard.js';
import * as moment from 'moment-timezone';
import { TranslateService } from '@ngx-translate/core';

import { MessageQueueService, MESSAGE_TO, NewUrlStateNotificationService, StoreHelperService, WebAppSettingDataService } from 'app/shared/services';
import { IUrlStatChartDataParams, UrlStatisticChartDataService } from './url-statistic-chart-data.service';
import { UrlPathId } from 'app/shared/models';
import { makeYData, makeXData, getMaxTickValue, getStackedData } from 'app/core/utils/chart-util';
import { isEmpty } from 'app/core/utils/util';

export enum Layer {
    LOADING = 'loading',
    RETRY = 'retry',
    CHART = 'chart'
}

@Component({
    selector: 'pp-url-statistic-chart-container',
    templateUrl: './url-statistic-chart-container.component.html',
	styleUrls: ['./url-statistic-chart-container.component.css']
})
export class UrlStatisticChartContainerComponent implements OnInit, OnDestroy {
    private clickTab = new Subject<string>();
    private onClickTab$ = this.clickTab.asObservable();
	private unsubscribe = new Subject<void>();
	private defaultYMax = 1;
    private chartColorList: string[];
    private timezone: string;
    private dateFormatMonth: string;
    private dateFormatDay: string;
	private cachedData: {
        [key: string]: { // url
            [key: string]: { // chart type (total, failure, ...)
                timestamp: number[],
                metricValues: IMetricValue[]
            }
        }
     } = {};
    private fieldNameList: string[];
    private previousParams: IUrlStatChartDataParams;
    
    isUriSelected: boolean;
    selectedUri = '';
	chartConfig: IChartConfig;
    showLoading: boolean;
    showRetry: boolean;
    retryMessage: string;
    guideMessage: string;
    emptyMessage: string;
    chartVisibility = {};
    _activeLayer: Layer;

    tabList: {id: string, display: string}[];
    activeTabId = 'total';

	constructor(
		private messageQueueService: MessageQueueService,
		private webAppSettingDataService: WebAppSettingDataService,
        private storeHelperService: StoreHelperService,
		private newUrlStateNotificationService: NewUrlStateNotificationService,
		private urlStatisticChartDataService: UrlStatisticChartDataService,
        private translateService: TranslateService,
        private el: ElementRef,
        private cd: ChangeDetectorRef
	) { }

	ngOnInit() {
        this.initFieldNameList();
		this.initChartColorList();
        this.initI18nText();
        this.initTabList();
        // this.initDefaultChartConfig();
        this.listenToEmitter();

		this.newUrlStateNotificationService.onUrlStateChange$.pipe(
			takeUntil(this.unsubscribe)
		).subscribe(() => {
			this.cachedData = {};
            // this.isUriSelected = false;
            this.selectedUri = null;
            // this.chartConfig = null;
            // this.initDefaultChartConfig();
		});

        combineLatest([
            this.messageQueueService.receiveMessage(this.unsubscribe, MESSAGE_TO.SELECT_URL_INFO).pipe(
                tap((uri: string) => {
                    this.selectedUri = uri;
                }),
            ),
            this.onClickTab$.pipe(
                startWith(this.activeTabId)
            )
        ]).pipe(
            filter(([uri, _]: string[]) => {
                if (Boolean(uri)) {
                    if (isEmpty(this.cachedData[uri])) {
                        this.cachedData[uri] = {};
                    }
                    return true;
                } else {
                    this.initDefaultChartConfig();
                    this.cd.detectChanges();
                    return false;
                }
            }),
            switchMap(([uri, tabId]: string[]) => {
				if (Boolean(this.cachedData[uri][tabId])) {
					return of(this.cachedData[uri][tabId]);
				} else {
					const urlService = this.newUrlStateNotificationService;
					const from = urlService.getStartTimeToNumber();
					const to = urlService.getEndTimeToNumber();
					const applicationName = urlService.getPathValue(UrlPathId.APPLICATION).getApplicationName();
					const agentId = urlService.getPathValue(UrlPathId.AGENT_ID) || '';
					const params = this.previousParams = {from, to, applicationName, agentId, uri, type: tabId};
                    
                    this.activeLayer = Layer.LOADING;
                    this.cd.detectChanges();

					return this.urlStatisticChartDataService.getData(params).pipe(
						map(({timestamp, metricValueGroups}: IUrlStatChartData) => {
							this.cachedData[uri][tabId] = {timestamp, metricValues: metricValueGroups[0].metricValues};
							return this.cachedData[uri][tabId];
						}),
                        catchError((error: IServerError) => {
                            this.activeLayer = Layer.RETRY;
                            this.setRetryMessage(error.message);

                            return EMPTY;
                        })
					);	
				}
			})
        ).subscribe((data: {timestamp: number[], metricValues: IMetricValue[]}) => {
			this.setChartConfig(this.makeChartData(data));
		});
	}

	ngOnDestroy(): void {
		this.unsubscribe.next();
		this.unsubscribe.complete();
	}

    set activeLayer(layer: Layer) {
        this._activeLayer = layer;
        this.setChartVisibility(this._activeLayer === Layer.CHART);
    }

    get activeLayer(): Layer {
        return this._activeLayer;
    }

    private setChartVisibility(showChart: boolean): void {
        this.chartVisibility = {
            'show-chart': showChart,
            'shady-chart': !showChart && this.chartConfig !== undefined,
        };
    }

    private initTabList(): void {
        this.tabList = [{
            id: 'total',
            display: 'Total Count',
        },
        {
            id: 'failure',
            display: 'Failure Count',
        }];
    }

    onTabClick(tabId: string): void {
        if (tabId === this.activeTabId) {
            return;
        }

        this.activeTabId = tabId;
        this.clickTab.next(tabId);
    }

    onRendered(): void {
        this.activeLayer = Layer.CHART;
	}

    isActiveLayer(layer: string): boolean {
        return this.activeLayer === layer;
    }

    onRetry(): void {
        this.activeLayer = Layer.LOADING;
        this.urlStatisticChartDataService.getData(this.previousParams).pipe(
            map(({timestamp, metricValueGroups}: IUrlStatChartData) => {
                this.cachedData[this.selectedUri][this.activeTabId] = {timestamp, metricValues: metricValueGroups[0].metricValues};
                return this.cachedData[this.selectedUri][this.activeTabId];
            }),
            catchError((error: IServerError) => {
                this.activeLayer = Layer.RETRY;
                this.setRetryMessage(error.message);
                return EMPTY;
            }),
        ).subscribe((data: {timestamp: number[], metricValues: IMetricValue[]}) => {
            this.setChartConfig(this.makeChartData(data));
        });
    }

    private initFieldNameList(): void {
        this.fieldNameList = this.webAppSettingDataService.getUrlStatFieldNameList();
    }

	private initChartColorList(): void {
        const computedStyle = getComputedStyle(this.el.nativeElement);

        this.chartColorList = [
            computedStyle.getPropertyValue('--chart-most-success'),
            computedStyle.getPropertyValue('--chart-success'),
            computedStyle.getPropertyValue('--chart-kinda-success'),
            computedStyle.getPropertyValue('--chart-almost-normal'),
            computedStyle.getPropertyValue('--chart-normal'),
            computedStyle.getPropertyValue('--chart-slow'),
            computedStyle.getPropertyValue('--chart-very-slow'),
            computedStyle.getPropertyValue('--chart-fail'),
        ]
    }

    private initI18nText(): void {
        forkJoin([
            this.translateService.get('URL_STAT.SELECT_URL_INFO'),
            this.translateService.get('COMMON.NO_DATA')
        ]).subscribe(([guideMessage, emptyMessage]: string[]) => {
            this.guideMessage = guideMessage;
            this.emptyMessage = emptyMessage;
        });
    }

    private initDefaultChartConfig(): void {
        this.chartConfig = {
            dataConfig: {
                x: 'x',
                columns: [],
                empty: {
                    label: {
                        text: this.selectedUri === '' ? this.guideMessage : this.emptyMessage
                    }
                },
                type: bar(),
                colors: this.fieldNameList.reduce((acc: {[key: string]: string}, curr: string, i: number) => {
                    return {...acc, [curr]: this.chartColorList[i]};
                }, {}),
                groups: [this.fieldNameList],
                order: null
            },
            elseConfig: this.makeElseOption()
        }
    }

    private listenToEmitter(): void {
        this.storeHelperService.getTimezone(this.unsubscribe).subscribe((timezone: string) => {
            this.timezone = timezone;
        });

        this.storeHelperService.getDateFormatArray(this.unsubscribe, 6, 7).subscribe(([dateFormatMonth, dateFormatDay]: string[]) => {
            this.dateFormatMonth = dateFormatMonth;
            this.dateFormatDay = dateFormatDay;
        });
    }

    private setRetryMessage(message: string): void {
        this.retryMessage = message;
    }

	private makeChartData({timestamp, metricValues}: {[key: string]: any}): PrimitiveArray[] {
		return [
            ['x', ...makeXData(timestamp)],
            ...metricValues.map(({values}: IMetricValue, i: number) => {
                return [this.fieldNameList[i], ...values.map((v: number) => v < 0 ? null : v)];
            })
        ];
    }

    private setChartConfig(data: PrimitiveArray[]): void {
        this.chartConfig =  {
            dataConfig: this.makeDataOption(data),
            // elseConfig: this.makeElseOption(getMaxTickValue(getStackedData(data), 1)),
            elseConfig: this.makeElseOption(),
        };
    }

    private makeDataOption(columns: PrimitiveArray[]): Data {
        const keyList = columns.slice(1).map(([key]: PrimitiveArray) => key as string);

        return {
            x: 'x',
            columns,
            empty: {
                label: {
                    text: this.emptyMessage
                }
            },
            // type: bar(),
            // type: areaStep(),
            // colors: keyList.reduce((acc: {[key: string]: string}, curr: string, i: number) => {
            //     return {...acc, [curr]: this.chartColorList[i]};
            // }, {}),
            // groups: [keyList],
            // order: null
        };
    }

    private makeElseOption(): {[key: string]: any} {
        return {
            bar: {
                width: {
                    ratio: 0.8
                }
            },
            padding: {
                top: 20,
                bottom: 20,
                right: 20
            },
            axis: {
                x: {
                    type: 'timeseries',
                    tick: {
                        count: 6,
                        show: false,
                        format: (time: Date) => {
                            return moment(time).tz(this.timezone).format(this.dateFormatMonth) + '\n' + moment(time).tz(this.timezone).format(this.dateFormatDay);
                        }
                    },
                    padding: {
                        left: 0,
                        right: 0
                    }
                },
                y: {
                    label: {
                        text: 'Total Count',
                        position: 'outer-middle'
                    },
                    tick: {
                        count: 2,
                        format: (v: number): string => this.convertWithUnit(v),
                    },
                    padding: {
                        // top: 0,
                        bottom: 0
                    },
                    min: 0,
                    // max: yMax,
                    default: [0, this.defaultYMax]
                }
            },
            grid: {
                y: {
                    show: true
                }
            },
            point: {
                show: false,
            },
            tooltip: {
                order: '',
                format: {
                    // value: (v: number): string => this.addComma(v.toString())
                    value: (v: number): string => this.convertWithUnit(v)
                }
            },
            transition: {
                duration: 0
            },
            zoom: {
                enabled: zoom()
            },
        };
    }

    private addComma(str: string): string {
        return str.replace(/(\d)(?=(?:\d{3})+(?!\d))/g, '$1,');
    }

    private convertWithUnit(value: number): string {
        const unitList = ['', 'K', 'M', 'G'];

        return [...unitList].reduce((acc: string, curr: string, i: number, arr: string[]) => {
            const v = Number(acc);

            return v >= 1000
                ? (v / 1000).toString()
                : (arr.splice(i + 1), Number.isInteger(v) ? `${v}${curr}` : `${v.toFixed(2)}${curr}`);
        }, value.toString());
    }
}
