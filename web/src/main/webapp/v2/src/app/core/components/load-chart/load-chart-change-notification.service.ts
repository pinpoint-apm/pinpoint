import { Injectable } from '@angular/core';
import { Subject, Observable } from 'rxjs';
import { filter } from 'rxjs/operators';

import { StoreHelperService, MessageQueueService, MESSAGE_TO, AgentHistogramDataService } from 'app/shared/services';
import { ServerMapData } from 'app/core/components/server-map/class/server-map-data.class';

export enum SOURCE_TYPE {
    MAIN = 'MAIN_SIDEBAR',
    FILTERED = 'FILTERED_MAP_SIDEBAR',
    INFO_PER_SERVER = 'INFO_PER_SERVER'
}

export interface ILoadChartNotificationData {
    chart: {
        labels: string[];
        keyValeus: {
            key: string;
            values: number[];
        }[];
    };
    hidden: boolean;
    error: boolean;
}

@Injectable()
export class LoadChartChangeNotificationService {
    private unsubscribe: Subject<null> = new Subject();
    private outMainSideBarChartData: Subject<ILoadChartNotificationData> = new Subject();
    private outFilteredMapSideBarChartData: Subject<any> = new Subject();
    private outInfoPerServerChartData: Subject<ILoadChartNotificationData> = new Subject();

    private timezone: string;
    private dateFormatMonth: string;
    private dateFormatDay: string;
    private serverMapData: ServerMapData;
    private selectedTarget: ISelectedTarget = null;
    private selectedAgent = '';
    private isOriginalNode = false;
    private yMax = -1;

    constructor(
        private storeHelperService: StoreHelperService,
        private messageQueueService: MessageQueueService,
        private agentHistogramDataService: AgentHistogramDataService
    ) {
        this.connectStore();
    }
    private connectStore(): void {
        this.storeHelperService.getTimezone(this.unsubscribe).subscribe((timezone: string) => {
            this.timezone = timezone;
        });
        this.storeHelperService.getDateFormatArray(this.unsubscribe, 6, 7).subscribe((dateFormat: string[]) => {
            this.dateFormatMonth = dateFormat[0];
            this.dateFormatDay = dateFormat[1];
        });
        this.storeHelperService.getServerMapData(this.unsubscribe).subscribe((serverMapData: ServerMapData) => {
            this.serverMapData = serverMapData;
        });
        this.storeHelperService.getServerMapTargetSelected(this.unsubscribe).pipe(
            filter((target: ISelectedTarget) => {
                return target && (target.isNode === true || target.isNode === false) ? true : false;
            })
        ).subscribe((target: ISelectedTarget) => {
            this.yMax = -1;
            this.isOriginalNode = true;
            this.selectedAgent = '';
            this.selectedTarget = target;
            if (target.isMerged === true) {
                const oData = {
                    chart: null,
                    error: false,
                    hidden: true
                };
                this.outMainSideBarChartData.next(oData);
                this.outFilteredMapSideBarChartData.next(oData);
            } else {
                this.loadChartData();
            }
        });
        this.storeHelperService.getAgentSelection(this.unsubscribe).subscribe((agent: string) => {
            this.selectedAgent = agent;
            if (this.selectedTarget) {
                this.loadChartData();
            }
        });
        this.storeHelperService.getServerMapTargetSelectedByList(this.unsubscribe).subscribe((target: any) => {
            this.isOriginalNode = this.selectedTarget.node[0] === target.key;
            const oData = {
                chart: this.agentHistogramDataService.makeChartDataForLoad(
                    target.timeSeriesHistogram,
                    this.timezone,
                    [this.dateFormatMonth, this.dateFormatDay],
                    this.getChartYMax()
                ),
                error: false,
                hidden: false
            };
            this.outMainSideBarChartData.next(oData);
            this.outFilteredMapSideBarChartData.next(oData);
        });
        this.storeHelperService.getLoadChartYMax(this.unsubscribe).subscribe((max: number) => {
            this.yMax = max;
        });
        this.storeHelperService.getAgentSelectionForServerList(this.unsubscribe).pipe(
            filter((chartData: IAgentSelection) => {
                return (chartData && chartData.agent) ? true : false;
            })
        ).subscribe((chartData: IAgentSelection) => {
            if (chartData.load) {
                this.outInfoPerServerChartData.next({
                    chart: this.agentHistogramDataService.makeChartDataForLoad(
                        chartData.load,
                        this.timezone,
                        [this.dateFormatMonth, this.dateFormatDay],
                        this.getChartYMax()
                    ),
                    error: false,
                    hidden: false
                });
            } else {
                this.outInfoPerServerChartData.next({
                    chart: null,
                    error: false,
                    hidden: false
                });
            }
        });
        this.messageQueueService.receiveMessage(this.unsubscribe, MESSAGE_TO.REAL_TIME_SCATTER_CHART_X_RANGE).subscribe(([{ from, to }]: IScatterXRange[]) => {
            this.yMax = -1;
            this.loadChartData(from, to);
        });
    }
    private loadChartData(from?: number, to?: number): void {
        const target = this.getTargetInfo();
        if (target) {
            if (this.isAllAgent() && arguments.length !== 2) {
                const oData = {
                    chart: this.agentHistogramDataService.makeChartDataForLoad(
                        target.timeSeriesHistogram,
                        this.timezone,
                        [this.dateFormatMonth, this.dateFormatDay],
                        this.getChartYMax()
                    ),
                    error: false,
                    hidden: false
                };
                this.outMainSideBarChartData.next(oData);
                this.outFilteredMapSideBarChartData.next(oData);
            } else {
                if (target['agentTimeSeriesHistogram']) {
                    this.outFilteredMapSideBarChartData.next({
                        chart: this.agentHistogramDataService.makeChartDataForLoad(
                            target['agentTimeSeriesHistogram'][this.selectedAgent],
                            this.timezone,
                            [this.dateFormatMonth, this.dateFormatDay],
                            this.getChartYMax()
                        ),
                        error: false,
                        hidden: false
                    });
                } else {
                    this.agentHistogramDataService.getData(target.key, target.applicationName, target.serviceTypeCode, this.serverMapData, from, to).subscribe((chartData: any) => {
                        const chartDataForAgent = this.isAllAgent() ? chartData['timeSeriesHistogram'] : chartData['agentTimeSeriesHistogram'][this.selectedAgent];
                        this.outMainSideBarChartData.next({
                            chart: this.agentHistogramDataService.makeChartDataForLoad(
                                chartDataForAgent,
                                this.timezone,
                                [this.dateFormatMonth, this.dateFormatDay],
                                this.getChartYMax()
                            ),
                            error: false,
                            hidden: false
                        });
                    }, (error: IServerErrorFormat) => {
                        this.outMainSideBarChartData.next({
                            chart: null,
                            error: true,
                            hidden: false
                        });
                    });
                }
            }
        }
    }
    private getChartYMax(): number {
        return this.isOriginalNode ? (this.yMax === -1 ? null : this.yMax) : null;
    }
    private isAllAgent(): boolean {
        return this.selectedAgent === '';
    }
    private getTargetInfo(): any {
        if (this.selectedTarget.isNode) {
            return this.serverMapData.getNodeData(this.selectedTarget.node[0]);
        } else {
            return this.serverMapData.getLinkData(this.selectedTarget.link[0]);
        }
    }
    getObservable(type: string): Observable<any> {
        switch (type) {
            case SOURCE_TYPE.MAIN:
                return this.outMainSideBarChartData.asObservable();
            case SOURCE_TYPE.FILTERED:
                return this.outFilteredMapSideBarChartData.asObservable();
            case SOURCE_TYPE.INFO_PER_SERVER:
                return this.outInfoPerServerChartData.asObservable();
            default:
                return this.outMainSideBarChartData.asObservable();
        }
    }
    setYMax(max: number): void {
        if (this.yMax === -1 && this.isOriginalNode) {
            this.yMax = max;
        }
    }
}
