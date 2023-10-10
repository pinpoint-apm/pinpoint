import { ChangeDetectorRef, Component, ComponentFactoryResolver, ComponentRef, ElementRef, HostBinding, OnDestroy, OnInit, ViewChild, ViewContainerRef } from '@angular/core';
import { combineLatest, EMPTY, forkJoin, of, Subject } from 'rxjs';
import { catchError, filter, map, startWith, switchMap, takeUntil, tap } from 'rxjs/operators';
import { TranslateService } from '@ngx-translate/core';

import { MessageQueueService, MESSAGE_TO, NewUrlStateNotificationService } from 'app/shared/services';
import { IUrlStatChartDataParams, UrlStatisticChartDataService } from './url-statistic-chart-data.service';
import { UrlPathId } from 'app/shared/models';
import { isEmpty } from 'app/core/utils/util';
import { UrlStatisticBarChartComponent } from './url-statistic-bar-chart.component';
import { UrlStatisticLineChartComponent } from './url-statistic-line-chart.component';

export enum Layer {
    LOADING = 'loading',
    RETRY = 'retry',
    CHART = 'chart'
}

enum ChartKey {
    TOTAL = 'total',
    FAILURE = 'failure',
    APDEX = 'apdex',
    LATENCY = 'latency'
}

enum ChartType {
    LINE = 'line',
    BAR = 'bar',
}

export type ChartConfig = {
    colors?: string[];
    valueFormat?: (v: number) => string;
    yAxis?: {
        label?: string;
    };
}

@Component({
    selector: 'pp-url-statistic-chart-container',
    templateUrl: './url-statistic-chart-container.component.html',
	styleUrls: ['./url-statistic-chart-container.component.css']
})
export class UrlStatisticChartContainerComponent implements OnInit, OnDestroy {
    @HostBinding('class') hostClass = 'url-statistic-chart-container';
    @ViewChild('chartContainer', {read: ViewContainerRef, static: false}) chartContainer: ViewContainerRef;

    private clickTab = new Subject<ChartKey>();
    private onClickTab$ = this.clickTab.asObservable();
	private unsubscribe = new Subject<void>();
	private cachedData: {
        [key: string]: { // url
            [key: string]: { // chart type (total, failure, ...)
                timestamp: number[],
                metricValues: IMetricValue[],
                chartType: string,
            }
        }
     } = {};
    private previousParams: IUrlStatChartDataParams;
    private componentRefMap = new Map<string, ComponentRef<UrlStatisticBarChartComponent | UrlStatisticLineChartComponent>>();
    
    selectedUri = '';
    showLoading: boolean;
    showRetry: boolean;
    retryMessage: string;
    guideMessage: string;
    emptyMessage: string;
    chartVisibility = {};
    _activeLayer: Layer;

    tabList: {id: string, display: string}[];
    activeTabId = ChartKey.TOTAL;
    defaultChartConfig: IChartConfig;
    chartConfig: Partial<Record<ChartKey, ChartConfig>>;
    defaultChartMessage: string;

	constructor(
		private messageQueueService: MessageQueueService,
		private newUrlStateNotificationService: NewUrlStateNotificationService,
		private urlStatisticChartDataService: UrlStatisticChartDataService,
        private translateService: TranslateService,
        private el: ElementRef,
        private cd: ChangeDetectorRef,
        private componentFactoryResolver: ComponentFactoryResolver,
	) { }

	ngOnInit() {
        this.initI18nText();
        this.initTabList();
        this.initChartConfig();

		this.newUrlStateNotificationService.onUrlStateChange$.pipe(
			takeUntil(this.unsubscribe),
		).subscribe(() => {
			this.cachedData = {};
		});

        combineLatest([
            this.messageQueueService.receiveMessage(this.unsubscribe, MESSAGE_TO.SELECT_URL_INFO).pipe(
                tap((uri: string) => {
                    this.selectedUri = uri;
                }),
            ),
            this.onClickTab$.pipe(
                startWith(this.activeTabId),
            )
        ]).pipe(
            filter(([uri, _]: string[]) => {
                if (Boolean(uri)) {
                    if (isEmpty(this.cachedData[uri])) {
                        this.cachedData[uri] = {};
                    }
                    return true;
                } else {
                    this.defaultChartMessage = uri === '' ? this.guideMessage : this.emptyMessage;
                    this.componentRefMap.clear();
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
							this.cachedData[uri][tabId] = {
                                timestamp,
                                metricValues: metricValueGroups[0].metricValues,
                                chartType: metricValueGroups[0].chartType
                            };
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
        ).subscribe((data: {timestamp: number[], metricValues: IMetricValue[], chartType: string}) => {
            this.loadComponent(this.activeTabId, data);
		});
	}

	ngOnDestroy(): void {
		this.unsubscribe.next();
		this.unsubscribe.complete();
        if (this.chartContainer) {
            this.chartContainer.get(0).destroy();
        }
	}

    private loadComponent(key: ChartKey, {timestamp, metricValues, chartType}: {timestamp: number[], metricValues: IMetricValue[], chartType: string}): void {
        const componentRef = this.componentRefMap.get(key);

        if (!componentRef) {
            let componentRef;

            if (chartType === ChartType.BAR) {
                const componentFactory = this.componentFactoryResolver.resolveComponentFactory(UrlStatisticBarChartComponent);
                componentRef = this.chartContainer.createComponent(componentFactory);
            } else {
                const componentFactory = this.componentFactoryResolver.resolveComponentFactory(UrlStatisticLineChartComponent);
                componentRef = this.chartContainer.createComponent(componentFactory);
            }

            const component = componentRef.instance;

            this.componentRefMap.set(key, componentRef);

            component.chartData = {
                x: timestamp,
                y: metricValues.reduce((acc: {[key: string]: number[]}, {fieldName, values}: IMetricValue) => {
                    return {...acc, [fieldName]: values.map((v: number) => v < 0 ? null : v)};
                }, {})
            };
            component.chartOptions = {
                type: chartType,
                ...this.chartConfig[key]
            }
            component.emptyMessage = this.emptyMessage;

            component.outRendered.subscribe(() => {
                this.onRendered();
            });
        } else {
            if (this.chartContainer.indexOf(componentRef.hostView) === -1) {
                this.chartContainer.insert(componentRef.hostView);
            }
            // TODO: Filter the case when the user just switches over the tab without clicking other uri data
            componentRef.instance.updateChart({
                x: timestamp,
                y: metricValues.reduce((acc: {[key: string]: number[]}, {fieldName, values}: IMetricValue) => {
                    return {...acc, [fieldName]: values.map((v: number) => v < 0 ? null : v)};
                }, {})
            });
        }
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
        this.tabList = [
            {
                id: 'total',
                display: 'Total Count',
            },
            {
                id: 'failure',
                display: 'Failure Count',
            },
            {
                id: 'apdex',
                display: 'Apdex',
            },
            {
                id: 'latency',
                display: 'Latency',
            },
        ];
    }

    onTabClick(tabId: ChartKey): void {
        if (tabId === this.activeTabId) {
            return;
        }

        this.activeTabId = tabId;
        if (this.chartContainer)  {
            this.chartContainer.detach(0);
        }
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
                this.cachedData[this.selectedUri][this.activeTabId] = {
                    timestamp,
                    metricValues: metricValueGroups[0].metricValues,
                    chartType: metricValueGroups[0].chartType
                };
                return this.cachedData[this.selectedUri][this.activeTabId];
            }),
            catchError((error: IServerError) => {
                this.activeLayer = Layer.RETRY;
                this.setRetryMessage(error.message);
                return EMPTY;
            }),
        ).subscribe((data: {timestamp: number[], metricValues: IMetricValue[], chartType: string}) => {
            this.loadComponent(this.activeTabId, data);
        });
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

    private initChartConfig(): void {
        const computedStyle = getComputedStyle(this.el.nativeElement);

        this.chartConfig = {
            [ChartKey.TOTAL]: {
                colors: [
                    computedStyle.getPropertyValue('--chart-most-success'),
                    computedStyle.getPropertyValue('--chart-success'),
                    computedStyle.getPropertyValue('--chart-kinda-success'),
                    computedStyle.getPropertyValue('--chart-almost-normal'),
                    computedStyle.getPropertyValue('--chart-normal'),
                    computedStyle.getPropertyValue('--chart-slow'),
                    computedStyle.getPropertyValue('--chart-very-slow'),
                    computedStyle.getPropertyValue('--chart-fail'),
                ],
                valueFormat: (v: number) => this.convertWithUnit(v),
                yAxis: {
                    label: 'Total Count'
                }
            },
            [ChartKey.FAILURE]: {
                colors: [
                    computedStyle.getPropertyValue('--chart-most-success'),
                    computedStyle.getPropertyValue('--chart-success'),
                    computedStyle.getPropertyValue('--chart-kinda-success'),
                    computedStyle.getPropertyValue('--chart-almost-normal'),
                    computedStyle.getPropertyValue('--chart-normal'),
                    computedStyle.getPropertyValue('--chart-slow'),
                    computedStyle.getPropertyValue('--chart-very-slow'),
                    computedStyle.getPropertyValue('--chart-fail'),
                ],
                valueFormat: (v: number) => this.convertWithUnit(v),
                yAxis: {
                    label: 'Failure Count'
                }
            },
            [ChartKey.APDEX]: {
                colors: [
                    '#41c464'
                ],
                valueFormat: (v) => this.numberInDecimal(v, 2),
                yAxis: {
                    label: 'Apdex Score'
                }
            },
            [ChartKey.LATENCY]: {
                colors: [
                    '#5d19a3',
                    '#0066CC'
                ],
                valueFormat: (v) => `${this.numberInInteger(v)}ms`,
                yAxis: {
                    label: 'Latency(ms)'
                }
            }
        }
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
    
    private numberInDecimal(v: number, decimalPlace: number): string {
        return (Math.floor(v * 100) / 100).toFixed(decimalPlace);
    }
    
    private numberInInteger(v: number): string {
        return Number(Math.round(v)).toLocaleString();
    }

    private setRetryMessage(message: string): void {
        this.retryMessage = message;
    }
}
