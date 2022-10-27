import { Injectable } from '@angular/core';
import { Observable, Subject, BehaviorSubject } from 'rxjs';

import {
    NewUrlStateNotificationService,
    WebAppSettingDataService,
    MessageQueueService,
    MESSAGE_TO
} from 'app/shared/services';
import { UrlPath, UrlPathId } from 'app/shared/models';
import { Timeline } from 'app/core/components/timeline/class';

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
    private sourceForTimeline = new Subject<ISourceForTimeline>();
    private sourceForAgentInfo = new Subject<ISourceForAgentInfo>();
    private sourceForChart = new Subject<ISourceForChart>();
    private sourceForTimelineCommand = new Subject<ISourceForTimelineCommand>();

    private timelineInfo: ITimelineInfo;
    private agentId: string;

    sourceForTimeline$: Observable<ISourceForTimeline>;
    sourceForAgentInfo$: Observable<ISourceForAgentInfo>;
    sourceForChart$: Observable<ISourceForChart>;
    sourceForTimelineCommand$: Observable<ISourceForTimelineCommand>;

    constructor(
        private newUrlStateNotificationService: NewUrlStateNotificationService,
        private webAppSettingDataService: WebAppSettingDataService,
        private messageQueueService: MessageQueueService,
    ) {
        this.sourceForTimeline$ = this.sourceForTimeline.asObservable();
        this.sourceForAgentInfo$ = this.sourceForAgentInfo.asObservable();
        this.sourceForChart$ = this.sourceForChart.asObservable();
        this.sourceForTimelineCommand$ = this.sourceForTimelineCommand.asObservable();
    }

    activate(unsubscribe: Subject<void>): void {
        this.messageQueueService.receiveMessage(unsubscribe, MESSAGE_TO.AGENT_LIST_VALID).subscribe(({range, now, agentId}: {range: number[], now: number, agentId: string}) => {
            this.agentId = agentId;
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

                this.messageQueueService.sendMessage({
                    to: MESSAGE_TO.FETCH_AGENT_LIST,
                    param: {
                        range: [from, to],
                        emitAfter
                    }
                })
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
