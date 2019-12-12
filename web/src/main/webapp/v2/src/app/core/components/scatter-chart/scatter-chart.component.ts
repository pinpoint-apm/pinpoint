import { Component, OnInit, OnDestroy, OnChanges, Input, Output, EventEmitter, ViewChild, ElementRef, SimpleChanges } from '@angular/core';
import { Subject } from 'rxjs';
import { takeUntil, filter } from 'rxjs/operators';

import { WindowRefService } from 'app/shared/services';
import { ScatterChart } from './class/scatter-chart.class';
import { ScatterChartDataBlock } from './class/scatter-chart-data-block.class';
import { ScatterChartInteractionService, IChangedViewTypeParam, IRangeParam, IResetParam, IChangedAgentParam } from './scatter-chart-interaction.service';

@Component({
    selector: 'pp-scatter-chart',
    templateUrl: './scatter-chart.component.html',
    styleUrls: ['./scatter-chart.component.css']
})
export class ScatterChartComponent implements OnInit, OnDestroy, OnChanges {
    @ViewChild('scatter', { static: true }) elementScatter: ElementRef;
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
    @Output() outTransactionCount: EventEmitter<object> = new EventEmitter();
    @Output() outSelectArea: EventEmitter<any> = new EventEmitter();
    @Output() outChangeRangeX: EventEmitter<any> = new EventEmitter();
    private readonly BLOCK_MAX_SIZE = 500;
    private unsubscribe: Subject<void> = new Subject();
    private hasError = false;
    dataLoaded = false;
    scatterChartInstance: ScatterChart = null;
    constructor(
        private windowRefService: WindowRefService,
        private scatterChartInteractionService: ScatterChartInteractionService
    ) {}
    ngOnInit() {}
    ngOnChanges(changes: SimpleChanges) {
        if (this.mode && (this.fromX >= 0) && (this.toX >= 0) && (this.fromY >= 0) && (this.toY >= 0) && this.application && this.timezone && this.dateFormat) {
            if (this.scatterChartInstance === null) {
                this.scatterChartInstance = new ScatterChart(
                    this.mode,
                    this.elementScatter.nativeElement,
                    this.fromX,
                    this.toX,
                    this.fromY,
                    this.toY,
                    this.application,
                    this.agent,
                    this.width,
                    this.height,
                    this.timezone,
                    this.dateFormat
                );
                this.addSubscribeForInstance();
                this.addSubscribeForService();
                this.addToWindow();
            } else {
                if (changes['timezone'] && changes['timezone'].currentValue) {
                    this.scatterChartInstance.setTimezone(this.timezone);
                    this.scatterChartInstance.redraw();
                }
                if (changes['dateFormat'] && changes['dateFormat'].currentValue) {
                    this.scatterChartInstance.setDateFormat(this.dateFormat);
                    this.scatterChartInstance.redraw();
                }
            }
        }
    }
    private addToWindow(): void {
        if (this.addWindow) {
            if ('scatterChartInstance' in this.windowRefService.nativeWindow === false) {
                this.windowRefService.nativeWindow['scatterChartInstance'] = {};
            }
            this.windowRefService.nativeWindow['scatterChartInstance'][this.application] = this.scatterChartInstance;
        }
    }
    private addSubscribeForInstance(): void {
        this.scatterChartInstance.onSelect$.pipe(
            takeUntil(this.unsubscribe)
        ).subscribe((area: any) => {
            this.outSelectArea.emit(area);
        });
        this.scatterChartInstance.onChangeTransactionCount$.pipe(
            takeUntil(this.unsubscribe)
        ).subscribe((typeCount: any) => {
            this.outTransactionCount.emit(typeCount);
        });
        this.scatterChartInstance.onChangeRangeX$.pipe(
            takeUntil(this.unsubscribe)
        ).subscribe((range: any) => {
            this.outChangeRangeX.emit(range);
        });
    }
    private addSubscribeForService(): void {
        this.scatterChartInteractionService.onChartData$.pipe(
            takeUntil(this.unsubscribe),
            filter((dataWrapper: any) => {
                return dataWrapper.instanceKey === this.instanceKey ? true : false;
            })
        ).subscribe((dataWrapper: {instanceKey: string, data: IScatterData}) => {
            const typeManager = this.scatterChartInstance.getTypeManager();
            const dataSize = dataWrapper.data.scatter.dotList.length;
            if (dataSize > this.BLOCK_MAX_SIZE) {
                const from = dataWrapper.data.from;
                const to =  dataWrapper.data.to;
                let rangeStart = 0;
                let rangeEnd = Math.min(dataSize, this.BLOCK_MAX_SIZE) - 1;
                do {
                    this.scatterChartInstance.addData(new ScatterChartDataBlock({
                        complete: false,
                        currentServerTime: dataWrapper.data.currentServerTime,
                        from: from,
                        resultFrom: dataWrapper.data.scatter.dotList[rangeEnd][0] + from,
                        resultTo: dataWrapper.data.scatter.dotList[rangeStart][0] + from,
                        scatter: {
                            dotList: dataWrapper.data.scatter.dotList.slice(rangeStart, rangeEnd + 1),
                            metadata: dataWrapper.data.scatter.metadata
                        },
                        to: to
                    }, typeManager));
                    rangeStart = rangeEnd + 1;
                    rangeEnd = Math.min(dataSize, rangeStart + this.BLOCK_MAX_SIZE) - 1;
                } while (rangeStart < dataSize);
            } else {
                this.scatterChartInstance.addData(new ScatterChartDataBlock(dataWrapper.data, typeManager));
            }
            this.dataLoaded = true;
            this.hasError = false;
        });
        this.scatterChartInteractionService.onViewType$.pipe(
            takeUntil(this.unsubscribe),
            filter((data: any) => {
                return data.instanceKey === this.instanceKey ? true : false;
            })
        ).subscribe((typeCheck: IChangedViewTypeParam) => {
            this.scatterChartInstance.changeShowType(typeCheck);
        });
        this.scatterChartInteractionService.onChangeYRange$.pipe(
            takeUntil(this.unsubscribe),
            filter((data: any) => {
                return data.instanceKey === this.instanceKey ? true : false;
            })
        ).subscribe((yRange: IRangeParam) => {
            this.scatterChartInstance.changeYRange(yRange);
        });
        this.scatterChartInteractionService.onSelectedAgent$.pipe(
            takeUntil(this.unsubscribe),
            filter((data: any) => {
                return data.instanceKey === this.instanceKey ? true : false;
            })
        ).subscribe((data: IChangedAgentParam) => {
            this.scatterChartInstance.changeSelectedAgent(data.agent);
        });
        this.scatterChartInteractionService.onInvokeDownloadChart$.pipe(
            takeUntil(this.unsubscribe),
            filter((instanceKey: string) => {
                return instanceKey === this.instanceKey ? true : false;
            })
        ).subscribe(() => {
            this.scatterChartInstance.downloadChartAsImage('png');
        });
        this.scatterChartInteractionService.onReset$.pipe(
            takeUntil(this.unsubscribe),
            filter((data: any) => {
                return data.instanceKey === this.instanceKey ? true : false;
            })
        ).subscribe((params: IResetParam) => {
            this.hasError = false;
            this.dataLoaded = false;
            this.application = params.application;
            this.agent = params.agent;
            this.fromX = params.from;
            this.toX = params.to;
            this.mode = params.mode;
            this.scatterChartInstance.reset(params.application, params.agent, params.from, params.to, params.mode);
            this.addToWindow();
        });
        this.scatterChartInteractionService.onError$.pipe(
            takeUntil(this.unsubscribe)
        ).subscribe((error: IServerErrorFormat) => {
            this.hasError = true;
            this.dataLoaded = true;
            this.scatterChartInstance.reset(this.application, this.agent, this.fromX, this.toX, this.mode);
            this.addToWindow();
        });
    }
    ngOnDestroy() {
        this.unsubscribe.next();
        this.unsubscribe.complete();
    }
    hideNoData(): boolean {
        if (this.dataLoaded === false) {
            return true;
        }
        if (this.mode === ScatterChart.MODE.REALTIME) {
            return true;
        }
        return this.scatterChartInstance && !this.scatterChartInstance.isEmpty();
    }
    getMessage(): string {
        return this.hasError ? this.i18nText.FAILED_TO_FETCH_DATA : this.i18nText.NO_DATA;
    }
}
