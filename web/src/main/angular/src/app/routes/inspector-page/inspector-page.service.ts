import { Injectable } from '@angular/core';
import { map, tap, takeUntil } from 'rxjs/operators';
import { Observable, Subject, BehaviorSubject } from 'rxjs';

import {
    NewUrlStateNotificationService,
    WebAppSettingDataService,
    MessageQueueService,
    MESSAGE_TO
} from 'app/shared/services';
import { UrlPath, UrlPathId } from 'app/shared/models';
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

export interface ISourceForTimelineCommand {
    selectedTime: number;
}

@Injectable()
export class InspectorPageService {
    private sourceForServerAndAgentList = new BehaviorSubject<ISourceForServerAndAgentList>(null);
    private sourceForTimeline = new Subject<ISourceForTimeline>();
    private sourceForAgentInfo = new Subject<ISourceForAgentInfo>();
    private sourceForChart = new Subject<ISourceForChart>();
    private sourceForTimelineCommand = new Subject<ISourceForTimelineCommand>();

    private timelineInfo: ITimelineInfo;
    private agentId: string;

    sourceForServerAndAgentList$: Observable<ISourceForServerAndAgentList>;
    sourceForTimeline$: Observable<ISourceForTimeline>;
    sourceForAgentInfo$: Observable<ISourceForAgentInfo>;
    sourceForChart$: Observable<ISourceForChart>;
    sourceForTimelineCommand$: Observable<ISourceForTimelineCommand>;

    constructor(
        private newUrlStateNotificationService: NewUrlStateNotificationService,
        private webAppSettingDataService: WebAppSettingDataService,
        private messageQueueService: MessageQueueService,
    ) {
        this.sourceForServerAndAgentList$ = this.sourceForServerAndAgentList.asObservable();
        this.sourceForTimeline$ = this.sourceForTimeline.asObservable();
        this.sourceForAgentInfo$ = this.sourceForAgentInfo.asObservable();
        this.sourceForChart$ = this.sourceForChart.asObservable();
        this.sourceForTimelineCommand$ = this.sourceForTimelineCommand.asObservable();
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

        this.messageQueueService.receiveMessage(unsubscribe, MESSAGE_TO.INSPECTOR_PAGE_VALID).subscribe(({range, now}: {range: number[], now: number}) => {
            this.notifyToTimeline(range);
            this.notifyToTimelineCommand();
            this.notifyToAgentInfo();
            this.notifyToChart();

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
    }

    updateTimelineData(data: ITimelineInfo): void {
        this.timelineInfo = data;
        this.notifyToTimelineCommand();
        this.notifyToAgentInfo();
        this.notifyToChart();
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
        if (this.shouldUpdateRange() || !this.timelineInfo) {
            this.timelineInfo = {
                range: this.calcuRetrieveTime(range),
                selectedTime: range[1],
                selectionRange: range
            };
        }

        this.sourceForTimeline.next({
            timelineInfo: this.timelineInfo,
            agentId: this.agentId
        });
    }

    private notifyToTimelineCommand(): void {
        this.sourceForTimelineCommand.next({
            selectedTime: this.timelineInfo.selectedTime
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
