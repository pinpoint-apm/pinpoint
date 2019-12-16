import { Injectable } from '@angular/core';
import { map, tap, filter, takeUntil } from 'rxjs/operators';
import { Observable, Subject, BehaviorSubject } from 'rxjs';

import {
    NewUrlStateNotificationService,
    StoreHelperService,
    WebAppSettingDataService,
    MessageQueueService,
    MESSAGE_TO
} from 'app/shared/services';
import { UrlPath, UrlPathId } from 'app/shared/models';
import { Actions } from 'app/shared/store';
import { Timeline } from 'app/core/components/timeline/class';

export interface ISourceForServerAndAgentList {
    range: number[];
    agentId: string;
    emitAfter: number;
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

@Injectable()
export class InspectorPageService {
    private sourceForServerAndAgentList = new BehaviorSubject<ISourceForServerAndAgentList>(null);
    private sourceForTimeline = new Subject<ISourceForTimeline>();
    private sourceForAgentInfo = new Subject<ISourceForAgentInfo>();
    private sourceForChart = new Subject<ISourceForChart>();

    private timelineInfo: ITimelineInfo;
    private agentId: string;
    private isFirstFlow: boolean; // AgentId가 invalid상태일때 redirect가 발생하는걸 detect하기위한 property

    sourceForServerAndAgentList$: Observable<ISourceForServerAndAgentList>;
    sourceForTimeline$: Observable<ISourceForTimeline>;
    sourceForAgentInfo$: Observable<ISourceForAgentInfo>;
    sourceForChart$: Observable<ISourceForChart>;

    constructor(
        private newUrlStateNotificationService: NewUrlStateNotificationService,
        private storeHelperService: StoreHelperService,
        private webAppSettingDataService: WebAppSettingDataService,
        private messageQueueService: MessageQueueService,
    ) {
        this.sourceForServerAndAgentList$ = this.sourceForServerAndAgentList.asObservable();
        this.sourceForTimeline$ = this.sourceForTimeline.asObservable();
        this.sourceForAgentInfo$ = this.sourceForAgentInfo.asObservable();
        this.sourceForChart$ = this.sourceForChart.asObservable();
    }

    activate(unsubscribe: Subject<void>): void {
        this.newUrlStateNotificationService.onUrlStateChange$.pipe(
            takeUntil(unsubscribe),
            tap((urlService: NewUrlStateNotificationService) => {
                this.agentId = urlService.getPathValue(UrlPathId.AGENT_ID);
            }),
            map((urlService: NewUrlStateNotificationService) => {
                if (urlService.isRealTimeMode()) {
                    const to = urlService.getUrlServerTimeData();
                    const from = to - this.webAppSettingDataService.getSystemDefaultPeriod().getMiliSeconds();

                    return [from, to];
                } else {
                    return [urlService.getStartTimeToNumber(), urlService.getEndTimeToNumber()];
                }

            })
        ).subscribe((range: number[]) => {
            this.notifyToServerAndAgentList(range);
        });

        this.messageQueueService.receiveMessage(unsubscribe, MESSAGE_TO.INSPECTOR_PAGE_VALID).pipe(
            tap(() => this.isFirstFlow = !this.timelineInfo)
        ).subscribe(({range, now}: {range: number[], now: number}) => {
            this.notifyToTimeline(range);
            if (!(this.isFirstFlow || this.shouldUpdateRange())) {
                this.notifyToAgentInfo();
                this.notifyToChart();
            }

            if (this.newUrlStateNotificationService.isRealTimeMode()) {
                const period = this.webAppSettingDataService.getChartRefreshInterval(UrlPath.INSPECTOR);
                const reservedNextTo = range[1] + period;
                const isDelayed = now > reservedNextTo;
                const to = isDelayed ? now : reservedNextTo;
                const from = to - this.webAppSettingDataService.getSystemDefaultPeriod().getMiliSeconds();
                const emitAfter = isDelayed ? 0 : reservedNextTo - now;

                this.notifyToServerAndAgentList([from, to], emitAfter);
            }
        });

        this.storeHelperService.getInspectorTimelineData(unsubscribe).pipe(
            filter(() => !this.isFirstFlow),
            tap((timelineInfo: ITimelineInfo) => this.timelineInfo = timelineInfo),
        ).subscribe(() => {
            this.notifyToAgentInfo();
            this.notifyToChart();
        });
    }

    updateTimelineData(data: ITimelineInfo): void {
        this.storeHelperService.dispatch(new Actions.UpdateTimelineData(data));
    }

    private calcuRetrieveTime([from, to]: number[]): number[] {
        const allowedMaxRagne = Timeline.MAX_TIME_RANGE;
        const timeGap = to - from;

        if (timeGap > allowedMaxRagne) {
            return [to - allowedMaxRagne, to];
        } else {
            const calcuStart = timeGap * 3;

            return [to - (calcuStart > allowedMaxRagne ? allowedMaxRagne : calcuStart), to];
        }
    }

    private shouldUpdateRange(): boolean {
        return this.newUrlStateNotificationService.isRealTimeMode()
            || this.newUrlStateNotificationService.isValueChanged(UrlPathId.PERIOD)
            || this.newUrlStateNotificationService.isValueChanged(UrlPathId.END_TIME);
    }

    private notifyToTimeline(range: number[]): void {
        if (this.isFirstFlow || this.shouldUpdateRange()) {
            this.updateTimelineData({
                range: this.calcuRetrieveTime(range),
                selectedTime: range[1],
                selectionRange: range
            });
        }

        this.sourceForTimeline.next({
            timelineInfo: this.timelineInfo,
            agentId: this.agentId
        });
    }

    private notifyToServerAndAgentList(range: number[], emitAfter = 0): void {
        this.sourceForServerAndAgentList.next({
            range,
            agentId: this.agentId,
            emitAfter
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
