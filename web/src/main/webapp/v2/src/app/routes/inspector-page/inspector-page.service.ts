import { Injectable } from '@angular/core';
import { switchMap, map, filter, tap, skip, delay, concatMap } from 'rxjs/operators';
import { iif, of, Observable, Subject, BehaviorSubject } from 'rxjs';

import { NewUrlStateNotificationService, StoreHelperService, WebAppSettingDataService, UrlRouteManagerService } from 'app/shared/services';
import { UrlPath, UrlPathId } from 'app/shared/models';
import { Actions } from 'app/shared/store';
import { ServerAndAgentListDataService } from './server-and-agent-list-data.service';
import { Timeline } from 'app/core/components/timeline/class';
import { filterServerList } from 'app/core/components/server-and-agent-list/server-and-agent-list-util';

export interface ISourceForServerAndAgentList {
    data: {[key: string]: IServerAndAgentData[]};
    agentId: string;
}

export interface ISourceForTimeline {
    timelineInfo: ITimelineInfo;
    agentId?: string;
}

export interface ISourceForAgentInfo {
    selectedTime: number;
    agentId: string;
}

export interface ISourceForChart {
    range: number[];
}

interface IRangeIter {
    emitAfter: number;
    endTime: number;
}

@Injectable()
export class InspectorPageService {
    private sourceForServerAndAgentList = new Subject<ISourceForServerAndAgentList>();
    private sourceForTimeline = new Subject<ISourceForTimeline>();
    private sourceForAgentInfo = new Subject<ISourceForAgentInfo>();
    private sourceForChart = new Subject<ISourceForChart>();

    private range: number[];
    private timelineInfo: ITimelineInfo;
    private agentId: string;
    private prevServerAndAgentList: {[key: string]: IServerAndAgentData[]};
    private isFirstFlow: boolean; // AgentId가 invalid상태일때 redirect가 발생하는걸 detect하기위한 property

    sourceForServerAndAgentList$: Observable<ISourceForServerAndAgentList>;
    sourceForTimeline$: Observable<ISourceForTimeline>;
    sourceForAgentInfo$: Observable<ISourceForAgentInfo>;
    sourceForChart$: Observable<ISourceForChart>;

    constructor(
        private newUrlStateNotificationService: NewUrlStateNotificationService,
        private storeHelperService: StoreHelperService,
        private webAppSettingDataService: WebAppSettingDataService,
        private serverAndAgentListDataService: ServerAndAgentListDataService,
        private urlRouteManagerService: UrlRouteManagerService,
    ) {
        this.sourceForServerAndAgentList$ = this.sourceForServerAndAgentList.asObservable();
        this.sourceForTimeline$ = this.sourceForTimeline.asObservable();
        this.sourceForAgentInfo$ = this.sourceForAgentInfo.asObservable();
        this.sourceForChart$ = this.sourceForChart.asObservable();
    }

    activate(unsubscribe: Subject<void>): void {
        this.newUrlStateNotificationService.onUrlStateChange$.pipe(
            tap((urlService: NewUrlStateNotificationService) => {
                this.agentId = urlService.getPathValue(UrlPathId.AGENT_ID);
            }),
            switchMap((urlService: NewUrlStateNotificationService) => {
                const appName = (urlService.getPathValue(UrlPathId.APPLICATION) as IApplication).getApplicationName();

                return iif(() => urlService.isRealTimeMode(),
                    (() => {
                        const rangeIter = new BehaviorSubject<IRangeIter>({emitAfter: 0, endTime: urlService.getUrlServerTimeData()});
                        const period = this.webAppSettingDataService.getChartRefreshInterval(UrlPath.INSPECTOR);

                        return rangeIter.pipe(
                            switchMap(({emitAfter, endTime}: IRangeIter) => {
                                return iif(() => emitAfter === 0,
                                    of(endTime),
                                    of(endTime).pipe(delay(emitAfter))
                                );
                            }),
                            map((to: number) => {
                                const from = to - this.webAppSettingDataService.getSystemDefaultPeriod().getMiliSeconds();

                                return [from, to];
                            }),
                            tap((range: number[]) => this.range = range),
                            concatMap((range: number[]) => {
                                return urlService.isValueChanged(UrlPathId.APPLICATION) || this.shouldUpdateRange()
                                    ? this.serverAndAgentListDataService.getData(appName, range).pipe(
                                        tap(() => {
                                            const now = Date.now();
                                            const reservedTo = range[1] + period;
                                            const isDelayed = now > reservedTo;

                                            rangeIter.next({
                                                emitAfter: isDelayed ? 0 : reservedTo - now,
                                                endTime: isDelayed ? now : reservedTo
                                            });
                                        })
                                    )
                                    : of(this.prevServerAndAgentList).pipe(delay(0));
                            })
                        );
                    })(),
                    (() => {
                        const range = this.range = [urlService.getStartTimeToNumber(), urlService.getEndTimeToNumber()];

                        return urlService.isValueChanged(UrlPathId.APPLICATION) || this.shouldUpdateRange()
                            ? this.serverAndAgentListDataService.getData(appName, range)
                            : of(this.prevServerAndAgentList).pipe(delay(0));
                    })()
                );
            }),
            tap((res: {[key: string]: IServerAndAgentData[]}) => this.prevServerAndAgentList = res),
            filter((res: {[key: string]: IServerAndAgentData[]}) => {
                if (this.agentId) {
                    const filteredList = filterServerList(res, this.agentId, ({ agentId }: IServerAndAgentData) => this.agentId.toLowerCase() === agentId.toLowerCase());
                    const isAgentIdValid = Object.keys(filteredList).length !== 0;

                    if (isAgentIdValid) {
                        return true;
                    } else {
                        const url = this.newUrlStateNotificationService.isRealTimeMode()
                            ? [ UrlPath.INSPECTOR, UrlPathId.REAL_TIME, this.newUrlStateNotificationService.getPathValue(UrlPathId.APPLICATION).getUrlStr() ]
                            : [
                                UrlPath.INSPECTOR,
                                this.newUrlStateNotificationService.getPathValue(UrlPathId.APPLICATION).getUrlStr(),
                                this.newUrlStateNotificationService.getPathValue(UrlPathId.PERIOD).getValueWithTime(),
                                this.newUrlStateNotificationService.getPathValue(UrlPathId.END_TIME).getEndTime()
                            ];

                        this.urlRouteManagerService.moveOnPage({ url });

                        return false;
                    }
                } else {
                    return true;
                }
            }),
            tap(() => {
                this.isFirstFlow = !this.timelineInfo;
            })
        ).subscribe((res: {[key: string]: IServerAndAgentData[]}) => {
            this.notifyToServerAndAgentList(res);
            this.notifyToTimeline();
            if (!(this.isFirstFlow || this.shouldUpdateRange())) {
                this.notifyToAgentInfo();
                this.notifyToChart();
            }
        });

        this.storeHelperService.getInspectorTimelineData(unsubscribe).pipe(
            skip(1),
            tap((timelineInfo: ITimelineInfo) => this.timelineInfo = timelineInfo),
        ).subscribe(() => {
            this.notifyToAgentInfo();
            this.notifyToChart();
        });
    }

    updateTimelineData(data: ITimelineInfo): void {
        this.storeHelperService.dispatch(new Actions.UpdateTimelineData(data));
    }

    private calcuRetrieveTime(startTime: number, endTime: number): number[] {
        const allowedMaxRagne = Timeline.MAX_TIME_RANGE;
        const timeGap = endTime - startTime;

        if (timeGap > allowedMaxRagne) {
            return [endTime - allowedMaxRagne, endTime];
        } else {
            const calcuStart = timeGap * 3;

            return [endTime - (calcuStart > allowedMaxRagne ? allowedMaxRagne : calcuStart), endTime];
        }
    }

    private shouldUpdateRange(): boolean {
        return this.newUrlStateNotificationService.isRealTimeMode()
            || this.newUrlStateNotificationService.isValueChanged(UrlPathId.PERIOD)
            || this.newUrlStateNotificationService.isValueChanged(UrlPathId.END_TIME);
    }

    private notifyToTimeline(): void {
        if (this.isFirstFlow || this.shouldUpdateRange()) {
            const [from, to] = this.range;

            this.updateTimelineData({
                range: this.calcuRetrieveTime(from, to),
                selectedTime: to,
                selectionRange: [from, to]
            });
        }

        this.sourceForTimeline.next({
            timelineInfo: this.timelineInfo,
            agentId: this.agentId
        });
    }

    private notifyToServerAndAgentList(data: {[key: string]: IServerAndAgentData[]}): void {
        this.sourceForServerAndAgentList.next({
            data,
            agentId: this.agentId
        });
    }

    private notifyToAgentInfo(): void {
        this.sourceForAgentInfo.next({
            selectedTime: this.timelineInfo.selectedTime,
            agentId: this.agentId
        });
    }

    private notifyToChart(): void {
        this.sourceForChart.next({
            range: this.timelineInfo.selectionRange
        });
    }
}
