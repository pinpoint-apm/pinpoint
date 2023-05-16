import {
    Component,
    OnInit,
    OnDestroy,
    OnChanges,
    Input,
    Output,
    EventEmitter,
    ViewChild,
    ElementRef,
    SimpleChanges,
    AfterViewInit,
    ChangeDetectorRef
} from '@angular/core';
import {from, Subject} from 'rxjs';
import {takeUntil, filter, map, pluck, switchMap, tap} from 'rxjs/operators';
import {ScatterChart} from '@pinpoint-fe/scatter-chart';
import * as moment from 'moment-timezone';

import {WindowRefService} from 'app/shared/services';
import {
    ScatterChartInteractionService,
    IRangeParam,
    IResetParam,
    IChangedAgentParam
} from './scatter-chart-interaction.service';
import {isEmpty} from 'app/core/utils/util';

const enum DataState {
    LOADING = 'LOADING',
    NO_DATA = 'NO_DATA',
    ERROR = 'ERROR',
    LOADED = 'LOADED'
}

interface IScatterDataType {
    x: number;
    y: number;
    type?: string;
    hidden?: boolean;
    metaInfo?: {
        transactionId?: string
    }
}

@Component({
    selector: 'pp-scatter-chart-v2',
    templateUrl: './scatter-chart-v2.component.html',
    styleUrls: ['./scatter-chart-v2.component.css']
})
export class ScatterChartV2Component implements OnInit, OnDestroy, OnChanges, AfterViewInit {
    @ViewChild('scatter', {static: true}) elementScatter: ElementRef;
    @Input() instanceKey: string;
    @Input() addWindow: boolean;
    @Input() width: number;
    @Input() height: number;
    @Input() fromX: number;
    @Input() toX: number;
    @Input() fromY: number;
    @Input() toY: number;
    @Input() mode: string;
    @Input() application: string;
    @Input() agent: string;
    @Input() i18nText: { [key: string]: string };
    @Input() timezone: string;
    @Input() dateFormat: string[];
    @Input() enableServerSideScan: boolean;
    @Input() backgroundColor: string;
    @Output() outSelectArea: EventEmitter<any> = new EventEmitter();
    @Output() outChangeRangeX: EventEmitter<any> = new EventEmitter();

    private unsubscribe = new Subject<void>();

    private SC: ScatterChart;
    private prevData: { [key: string]: IScatterDataType[] } = {};

    private computedStyle = getComputedStyle(document.body);
    private checkedType = ['success', 'failed'];

    dataState: DataState = DataState.LOADING;
    scatterChartInstance: ScatterChart = null;

    constructor(
        private windowRefService: WindowRefService,
        private scatterChartInteractionService: ScatterChartInteractionService,
        private cd: ChangeDetectorRef,
    ) {
    }

    ngOnInit() {
    }

    ngOnChanges(changes: SimpleChanges) {
        if (this.mode && (this.fromX >= 0) && (this.toX >= 0) && (this.fromY >= 0) && (this.toY >= 0) && this.application && this.timezone && this.dateFormat) {
            // TODO: Consider changing above if condition to selectedApplication(changes['application'])
            if (this.SC === undefined) {
                this.SC = new ScatterChart(
                    this.elementScatter.nativeElement,
                    {
                        axis: {
                            x: {
                                min: this.fromX,
                                max: this.toX,
                                tick: {
                                    width: 10,
                                    format: (value: number) => moment(value).tz(this.timezone).format(this.dateFormat[0]) + '\n' + moment(value).tz(this.timezone).format(this.dateFormat[1])
                                }
                            },
                            y: {
                                min: this.fromY,
                                max: this.toY,
                                tick: {
                                    width: 10,
                                    format: (value: number) => value.toLocaleString(),
                                },
                            }
                        },
                        background: {
                            color: this.backgroundColor || this.computedStyle.getPropertyValue('--background-default')
                        },
                        data: [
                            {
                                type: 'success',
                                color: this.computedStyle.getPropertyValue('--chart-success').trim(),
                                priority: 2,
                            },
                            {
                                type: 'failed',
                                color: this.computedStyle.getPropertyValue('--chart-fail').trim(),
                                priority: 1,
                                opacity: 1
                            }
                        ],
                        grid: {
                            strokeColor: this.computedStyle.getPropertyValue('--chart-guide-line')
                        },
                        legend: {
                            formatLabel: (label: string) => `${label.charAt(0).toUpperCase()}${label.slice(1)}`,
                            formatValue: (value: number) => value.toLocaleString(),
                        },
                        point: {
                            opacity: 0.5
                        },
                        render: {
                            drawOutOfRange: true
                        },
                        padding: {
                            left: 5
                        }
                    }
                );

                this.addSubscribeForInstance();
                this.addSubscribeForService();
                this.addToWindow();

                // this.SC.stopRealtime();
                if (this.mode === 'realtime') {
                    this.SC.startRealtime(this.toX - this.fromX);
                }
            } else {
            }
        }
    }

    ngAfterViewInit() {
    }

    getDataByRange(x1: number, x2: number, y1: number, y2: number, agentId: string, dotStatus: string[]): [string, number, number][] {
        const dotList = agentId ? this.prevData[agentId] : Object.values(this.prevData).flat();

        return dotList
            .filter(({
                         x,
                         y,
                         type
                     }: IScatterDataType) => (x1 <= x && x <= x2) && (y1 <= y && y <= y2) && dotStatus.includes(type))
            .map(({x, y, metaInfo}: IScatterDataType) => [metaInfo.transactionId, x, y]);
    }

    private addToWindow(): void {
        if (this.addWindow && !this.enableServerSideScan) {
            if ('scatterChartInstance' in this.windowRefService.nativeWindow === false) {
                this.windowRefService.nativeWindow['scatterChartInstance'] = {};
            }
            this.windowRefService.nativeWindow['scatterChartInstance'][this.application] = this;
        }
    }

    private addSubscribeForInstance(): void {
        this.SC.on('dragEnd', (_, {x1, x2, y1, y2}: any) => {
            this.outSelectArea.emit({
                x: {from: Math.round(x1), to: Math.round(x2)},
                y: {from: Math.round(y2), to: y1 > this.toY ? Number.MAX_SAFE_INTEGER : Math.round(y1)},
                type: this.checkedType
            })
        });

        this.SC.on('clickLegend', (_, {checked}) => {
            this.checkedType = checked; // ['success', 'failed']
        })
    }

    private addSubscribeForService(): void {
        this.scatterChartInteractionService.onChartData$.pipe(
            takeUntil(this.unsubscribe),
            filter((dataWrapper: any) => {
                return dataWrapper.instanceKey === this.instanceKey ? true : false;
            }),
            pluck('data'),
            map(({scatter, from}: IScatterData) => {
                /**
                 * * prevData format
                 * * {
                 * *    agent_1: [dot1, dot2, dot3, dot4],
                 * *    agent_2: [dot1, dot2, dot3, dot4],
                 * *    ...
                 * * }
                 */
                const currDotList: IScatterDataType[] = [];

                this.prevData = scatter.dotList.reduce((acc: any, curr: number[]) => {
                    const [x, y, agentIdIndex, partialTxId_3, typeNum, groupCount] = curr;
                    const [agentId, partialTxId_1, partialTxId_2] = scatter.metadata[agentIdIndex];
                    const dot = {
                        x: x + from,
                        y,
                        type: typeNum === 1 ? 'success' : 'failed',
                        hidden: groupCount === 0,
                        metaInfo: {
                            transactionId: `${partialTxId_1}^${partialTxId_2}^${partialTxId_3}`
                        }
                    };

                    if (this.agent === '' || this.agent === agentId) {
                        currDotList.push(dot);
                    }

                    if (acc[agentId]) {
                        acc[agentId].push(dot);
                    } else {
                        acc[agentId] = [dot];
                    }

                    return acc;
                }, this.prevData);


                return currDotList;
            }),
        ).subscribe((data: IScatterDataType[]) => {
            this.SC.render(data, {append: true});
            this.dataState = isEmpty(data) && this.mode !== 'realtime' ? DataState.NO_DATA : DataState.LOADED;
            this.cd.detectChanges();
        });
        this.scatterChartInteractionService.onChangeYRange$.pipe(
            takeUntil(this.unsubscribe),
            filter((data: any) => {
                return data.instanceKey === this.instanceKey ? true : false;
            })
        ).subscribe(({from, to}: IRangeParam) => {
            this.SC.setOption({
                axis: {
                    y: {
                        min: from,
                        max: to,
                    }
                }
            });
        });
        this.scatterChartInteractionService.onSelectedAgent$.pipe(
            takeUntil(this.unsubscribe),
            filter((data: any) => {
                return data.instanceKey === this.instanceKey ? true : false;
            }),
            map(({agent}: IChangedAgentParam) => {
                return agent ? this.prevData[agent] : Object.values(this.prevData).flat();
            })
        ).subscribe((data: IScatterDataType[] = []) => {
            if (this.mode === 'realtime') {
                const to = Date.now();
                const from = to - 300000;

                this.SC.setOption({
                    axis: {
                        x: {
                            min: from,
                            max: to,
                        }
                    }
                });
                this.SC.stopRealtime();
                this.SC.startRealtime(to - from);
            }
            this.dataState = isEmpty(data) && this.mode !== 'realtime' ? DataState.NO_DATA : DataState.LOADED;
            this.SC.render(data);
        });

        this.scatterChartInteractionService.onInvokeDownloadChart$.pipe(
            takeUntil(this.unsubscribe),
            filter((instanceKey: string) => {
                return instanceKey === this.instanceKey ? true : false;
            }),
            switchMap(() => from(this.SC.toBase64Image()))
        ).subscribe((img: string) => {
            let downloadElement = document.createElement('a');

            downloadElement.setAttribute('href', img);
            downloadElement.setAttribute('download', `Pinpoint_Scatter_Chart.png`);
            downloadElement.click();

            downloadElement = null;
        });
        this.scatterChartInteractionService.onReset$.pipe(
            takeUntil(this.unsubscribe),
            filter((data: any) => {
                return data.instanceKey === this.instanceKey ? true : false;
            }),
            tap(() => {
                this.prevData = {};
            })
        ).subscribe(({from, to}: IResetParam) => {
            this.dataState = DataState.LOADING;
            this.SC.clear();
            this.SC.setOption({
                axis: {
                    x: {
                        min: from,
                        max: to,
                    }
                }
            });

            this.SC.stopRealtime();
            if (this.mode === 'realtime') {
                this.SC.startRealtime(to - from);
            }
            this.addToWindow();
        });
        this.scatterChartInteractionService.onError$.pipe(
            takeUntil(this.unsubscribe)
        ).subscribe((error: IServerError) => {
            this.dataState = DataState.ERROR;
            this.SC.clear();
            this.SC.stopRealtime();
            // this.addToWindow();
        });
    }

    ngOnDestroy() {
        this.unsubscribe.next();
        this.unsubscribe.complete();
    }
}
