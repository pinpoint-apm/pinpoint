import { Component, OnInit, OnDestroy, Input, ComponentFactoryResolver, Injector, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { Subject, forkJoin, of, merge, EMPTY } from 'rxjs';
import { filter, tap, switchMap, pluck, map, catchError, withLatestFrom, takeUntil } from 'rxjs/operators';
import { TranslateService } from '@ngx-translate/core';
import { PrimitiveArray, Data, DataItem, bar } from 'billboard.js';

import {
    WebAppSettingDataService,
    AnalyticsService,
    TRACKED_EVENT_LIST,
    DynamicPopupService,
    AgentHistogramDataService,
    StoreHelperService,
    MessageQueueService,
    NewUrlStateNotificationService,
    MESSAGE_TO
} from 'app/shared/services';
import { HELP_VIEWER_LIST, HelpViewerPopupContainerComponent } from 'app/core/components/help-viewer-popup/help-viewer-popup-container.component';
import { ServerMapData } from 'app/core/components/server-map/class/server-map-data.class';
import { getMaxTickValue } from 'app/core/utils/chart-util';
import { Actions } from 'app/shared/store/reducers';
import { ServerErrorPopupContainerComponent } from 'app/core/components/server-error-popup/server-error-popup-container.component';

export enum SourceType {
    MAIN = 'MAIN',
    FILTERED = 'FILTERED',
    INFO_PER_SERVER = 'INFO_PER_SERVER'
}

export enum Layer {
    LOADING = 'loading',
    RETRY = 'retry',
    CHART = 'chart'
}

@Component({
    selector: 'pp-response-summary-chart-container',
    templateUrl: './response-summary-chart-container.component.html',
    styleUrls: ['./response-summary-chart-container.component.css'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class ResponseSummaryChartContainerComponent implements OnInit, OnDestroy {
    @Input() sourceType: string;

    private unsubscribe = new Subject<void>();
    private serverMapData: ServerMapData;
    private isOriginalNode: boolean;
    private selectedAgent = '';
    private chartColors: string[];
    private defaultYMax = 10;

    private originalTarget: ISelectedTarget; // from serverMap
    private targetFromList: INodeInfo; // from targetList

    dataFetchFailedText: string;
    dataEmptyText: string;
    chartConfig: IChartConfig;
    showLoading: boolean;
    showRetry: boolean;
    retryMessage: string;
    chartVisibility = {};
    previousRange: number[];

    constructor(
        private storeHelperService: StoreHelperService,
        private messageQueueService: MessageQueueService,
        private agentHistogramDataService: AgentHistogramDataService,
        private newUrlStateNotificationService: NewUrlStateNotificationService,
        private translateService: TranslateService,
        private webAppSettingDataService: WebAppSettingDataService,
        private analyticsService: AnalyticsService,
        private dynamicPopupService: DynamicPopupService,
        private injector: Injector,
        private componentFactoryResolver: ComponentFactoryResolver,
        private cd: ChangeDetectorRef,
    ) {}

    ngOnInit() {
        this.initChartColors();
        this.initI18nText();
        this.listenToEmitter();
    }

    ngOnDestroy() {
        this.unsubscribe.next();
        this.unsubscribe.complete();
    }

    set activeLayer(layer: Layer) {
        this.showLoading = layer === Layer.LOADING;
        this.showRetry = layer === Layer.RETRY;
        this.setChartVisibility(layer === Layer.CHART);
        this.cd.markForCheck();
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

    onRetry(): void {
        this.activeLayer = Layer.LOADING;
        const target = this.getTargetInfo();

        this.agentHistogramDataService.getData(this.serverMapData, this.previousRange, target).pipe(
            // map((data: any) => this.isAllAgent() ? data['histogram'] : data['agentHistogram'][this.selectedAgent])
            pluck('agentHistogram', this.selectedAgent),
            catchError((error: IServerError) => {
                this.activeLayer = Layer.RETRY;
                this.dynamicPopupService.openPopup({
                    data: {
                        title: 'Error',
                        contents: error
                    },
                    component: ServerErrorPopupContainerComponent
                }, {
                    resolver: this.componentFactoryResolver,
                    injector: this.injector
                });

                return EMPTY;
            })
        ).pipe(
            map((data: IResponseTime | IResponseMilliSecondTime) => this.makeChartData(data)),
            withLatestFrom(this.storeHelperService.getResponseSummaryChartYMax(this.unsubscribe))
        ).subscribe(([chartData, yMax]: [PrimitiveArray[], number]) => {
            this.chartConfig = {
                dataConfig: this.makeDataOption(chartData),
                elseConfig: this.makeElseOption(yMax)
            };

            this.cd.markForCheck();
        });
    }

    private initChartColors(): void {
        this.chartColors = this.webAppSettingDataService.getColorByRequest();
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

    private listenToEmitter(): void {
        this.newUrlStateNotificationService.onUrlStateChange$.pipe(
            takeUntil((this.unsubscribe)),
        ).subscribe(() => {
            this.serverMapData = null;
            this.originalTarget = null;
            this.targetFromList = null;
        });

        merge(
            this.messageQueueService.receiveMessage(this.unsubscribe, MESSAGE_TO.SERVER_MAP_TARGET_SELECT).pipe(
                filter(() => this.sourceType !== SourceType.INFO_PER_SERVER),
                filter((target: ISelectedTarget) => {
                    this.isOriginalNode = true;
                    this.selectedAgent = '';
                    this.originalTarget = target;
                    this.targetFromList = null;

                    return !target.isMerged;
                }),
                map(() => this.getTargetInfo().histogram),
            ),
            this.storeHelperService.getAgentSelection(this.unsubscribe).pipe(
                filter(() => this.sourceType !== SourceType.INFO_PER_SERVER),
                filter((agent: string) => this.selectedAgent !== agent),
                tap((agent: string) => this.selectedAgent = agent),
                filter(() => !!this.originalTarget),
                map(() => this.getTargetInfo()),
                switchMap((target: INodeInfo) => {
                    if (this.isAllAgent()) {
                        return of(target.histogram);
                    } else {
                        let data;

                        if (this.sourceType === SourceType.MAIN) {
                            const range = this.previousRange = this.newUrlStateNotificationService.isRealTimeMode()
                                ? this.previousRange ? this.previousRange : [this.newUrlStateNotificationService.getStartTimeToNumber(), this.newUrlStateNotificationService.getEndTimeToNumber()]
                                : [this.newUrlStateNotificationService.getStartTimeToNumber(), this.newUrlStateNotificationService.getEndTimeToNumber()];

                            data = this.agentHistogramDataService.getData(this.serverMapData, range, target).pipe(
                                catchError(() => of(null)),
                                filter((res: any) => res === null ? (this.activeLayer = Layer.RETRY, false) : true)
                            );
                        } else {
                            data = of(target);
                        }

                        return data.pipe(
                            pluck('agentHistogram', this.selectedAgent)
                        );
                    }
                }),
            ),
            this.messageQueueService.receiveMessage(this.unsubscribe, MESSAGE_TO.SERVER_MAP_TARGET_SELECT_BY_LIST).pipe(
                filter(() => this.sourceType !== SourceType.INFO_PER_SERVER),
                tap((target: any) => {
                    this.selectedAgent = '';
                    this.targetFromList = target;
                    if (this.originalTarget.isNode) {
                        // TODO: Need to update the ymax when directly selecting the input link before the related node.
                        // this.isOriginalNode = this.originalTarget.node.includes(target.key);
                        this.isOriginalNode = true;
                    } else {
                        this.isOriginalNode = true;
                    }
                }),
                map((target: any) => target.histogram),
            ),
            this.messageQueueService.receiveMessage(this.unsubscribe, MESSAGE_TO.AGENT_SELECT_FOR_SERVER_LIST).pipe(
                filter(() => this.sourceType === SourceType.INFO_PER_SERVER),
                tap(({agent}: IAgentSelection) => this.selectedAgent = agent),
                pluck('responseSummary'),
            ),
            this.messageQueueService.receiveMessage(this.unsubscribe, MESSAGE_TO.SERVER_MAP_DATA_UPDATE).pipe(
                tap(({serverMapData, range}: {serverMapData: ServerMapData, range: number[]}) => {
                    this.serverMapData = serverMapData;
                    this.previousRange = range;
                }),
                filter(() => !!this.originalTarget),
                filter(() => this.sourceType === SourceType.MAIN),
                filter(() => !this.originalTarget.isMerged || !!this.targetFromList),
                switchMap(({serverMapData, range}: {serverMapData: ServerMapData, range: number[]}) => {
                    const target = this.originalTarget.isMerged || this.targetFromList ? serverMapData.getNodeData(this.targetFromList.key) || serverMapData.getLinkData(this.targetFromList.key)
                        : this.getTargetInfo();

                    return !target ? of(null)
                        : this.selectedAgent ? this.agentHistogramDataService.getData(serverMapData, range, target).pipe(
                            catchError(() => of(null)),
                            filter((res: any) => !!res),
                            pluck('agentHistogram', this.selectedAgent)
                        )
                        : of (target.histogram);
                    // Consider reusing the below flow after investigating the api
                    // return isRemoved ? of(null) : this.agentHistogramDataService.getData(serverMapData, range, target).pipe(
                    //     catchError(() => of(null)),
                    //     filter((res: any) => !!res),
                    //     map((data: any) => this.isAllAgent() ? data['histogram'] : data['agentHistogram'][this.selectedAgent])
                    // );
                }),
            )
        ).pipe(
            map((data) => this.makeChartData(data)),
            switchMap((data: PrimitiveArray[]) => {
                if (this.shouldUpdateYMax()) {
                    const maxTickValue = getMaxTickValue(data, 1);
                    const yMax = maxTickValue === 0 ? this.defaultYMax : maxTickValue;

                    this.storeHelperService.dispatch(new Actions.UpdateResponseSummaryChartYMax(yMax));

                    return of([data, yMax]);
                } else {
                    return of(data).pipe(
                        withLatestFrom(this.storeHelperService.getResponseSummaryChartYMax(this.unsubscribe))
                    );
                }
            }),
        ).subscribe(([chartData, yMax]: [PrimitiveArray[], number]) => {
            this.chartConfig = {
                dataConfig: this.makeDataOption(chartData),
                elseConfig: this.makeElseOption(yMax),
            };

            this.cd.markForCheck();
        });
    }

    private shouldUpdateYMax(): boolean {
        return this.isAllAgent() && this.isOriginalNode;
    }

    private isAllAgent(): boolean {
        return this.selectedAgent === '';
    }

    private getTargetInfo(): any {
        return this.originalTarget.isNode
            ? this.serverMapData.getNodeData(this.originalTarget.node[0])
            : this.serverMapData.getLinkData(this.originalTarget.link[0]);
    }

    private makeChartData(data: IResponseTime | IResponseMilliSecondTime): PrimitiveArray[] {
        return data
            ? [['x', ...Object.keys(data)], ['rs', ...Object.values(data)]]
            : [];
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
            type: bar(),
            color: (_, {index}: DataItem): string => this.chartColors[index],
            labels: {
                colors: getComputedStyle(document.body).getPropertyValue('--text-primary'),
                format: {
                    rs: (v: number) => this.addComma(v.toString())
                }
            }
        };
    }

    private makeElseOption(yMax: number): {[key: string]: any} {
        return {
            padding: {
                top: 20
            },
            legend: {
                show: false
            },
            axis: {
                x: {
                    type: 'category'
                },
                y: {
                    tick: {
                        count: 3,
                        format: (v: number): string => this.convertWithUnit(v)
                    },
                    padding: {
                        top: 0,
                        bottom: 0
                    },
                    min: 0,
                    max: yMax,
                    default: [0, this.defaultYMax]
                }
            },
            grid: {
                y: {
                    show: true
                }
            },
            tooltip: {
                show: false
            },
            transition: {
                duration: 0
            }
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

    onClickColumn(columnName: string): void {
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.CLICK_RESPONSE_GRAPH);
        if (columnName === 'Error') {
            // scope.$emit('responseTimeSummaryChartDirective.showErrorTransactionList', type);
            // @TODO Scatter Chart의 에러 부분만 Drag 하도록 하는 액션
        }
        // @TODO FilteredMap transaction에서 만 처리되는 이벤트
        // if (useFilterTransaction) {
        //     scope.$emit('responseTimeSummaryChartDirective.itemClicked.' + scope.namespace, {
        //         "responseTime": type,
        //         "count": aTarget[0]._chart.config.data.datasets[0].data[aTarget[0]._index]
        //     });
        // }

    }

    onShowHelp($event: MouseEvent): void {
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.TOGGLE_HELP_VIEWER, HELP_VIEWER_LIST.RESPONSE_SUMMARY);
        const {left, top, width, height} = ($event.target as HTMLElement).getBoundingClientRect();

        this.dynamicPopupService.openPopup({
            data: HELP_VIEWER_LIST.RESPONSE_SUMMARY,
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
