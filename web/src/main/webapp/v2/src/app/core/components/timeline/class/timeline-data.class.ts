import { BehaviorSubject } from 'rxjs';

export interface ITimelineData {
    agentEventTimeline: IAgentEventTimeline;
    agentStatusTimeline: IAgentStatusTimeline;
}
export interface IAgentEventTimeline {
    timelineSegments: ITimelineEventSegment[];
}
export interface ITimelineEventSegment {
    startTimestamp: number;
    endTimestamp: number;
    value: {
        totalCount: number;
        typeCounts: ITimelineEventSegmentTypeCount[];
    };
}
export interface ITimelineEventSegmentTypeCount {
    code: number;
    desc: string;
    count: number;
}
export interface IAgentStatusTimeline {
    includeWarning: boolean;
    timelineSegments: ITimelineStatusSegment[];
}
export interface ITimelineStatusSegment {
    endTimestamp: number;
    startTimestamp: number;
    value: string;
}

export class TimelineData {
    aStatusRawData: ITimelineStatusSegment[];
    oStatusRawHash: { [key: string]: ITimelineStatusSegment };
    aEventRawData: ITimelineEventSegment[];

    onChangeStatusData$: BehaviorSubject<ITimelineStatusSegment[]> = new BehaviorSubject(null);
    onChangeEventData$: BehaviorSubject<ITimelineEventSegment[]> = new BehaviorSubject(null);
    constructor(aRawData: ITimelineData) {
        this.initData(aRawData);
        this.onChangeStatusData$.next(this.aStatusRawData);
        this.onChangeEventData$.next(this.aEventRawData);
    }
    initData(aRawData: ITimelineData): void {
        this.initStatusData(aRawData.agentStatusTimeline);
        this.initEventData(aRawData.agentEventTimeline);
    }
    initStatusData(oStatusRawData: IAgentStatusTimeline): void {
        this.aStatusRawData = oStatusRawData.timelineSegments || [];
        this.oStatusRawHash = {};
        this.aStatusRawData.forEach((segment: ITimelineStatusSegment) => {
            this.oStatusRawHash[this.makeID(segment)] = segment;
        });
    }
    initEventData(oEventRawData: IAgentEventTimeline): void {
        this.aEventRawData = oEventRawData.timelineSegments || [];
    }
    makeID(segment: ITimelineStatusSegment): string {
        return segment.endTimestamp + '';
    }
    eventCount(): number {
        return this.aEventRawData.length;
    }
    statusCount(): number {
        return this.aStatusRawData.length;
    }
    getDataByIndex(index: number): ITimelineStatusSegment {
        return this.aStatusRawData[index];
    }
    getDataByKey(key: string): ITimelineStatusSegment {
        return this.oStatusRawHash[key];
    }
    getEventDataByIndex(index: number): ITimelineEventSegment {
        return this.aEventRawData[index];
    }
    emptyData(): void {
        this.aStatusRawData = [];
        this.oStatusRawHash = {};
        this.aEventRawData = [];
    }
    addData(oNewData: ITimelineData): void {
        this.initData(oNewData);
        this.onChangeStatusData$.next(this.aStatusRawData);
        this.onChangeEventData$.next(this.aEventRawData);
    }
    destroy(): void {}
}
