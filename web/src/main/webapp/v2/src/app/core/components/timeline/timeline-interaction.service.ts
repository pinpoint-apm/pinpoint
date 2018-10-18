import { Injectable } from '@angular/core';
import { Subject, Observable } from 'rxjs';
import { ITimelineEventSegment } from './class/timeline-data.class';
export enum TimelineCommand {
    zoomIn = 'ZOOM_IN',
    zoomOut = 'ZOOM_OUT',
    prev = 'PREV',
    next = 'NEXT',
    now = 'NOW'
}

export interface ITimelineCommandParam {
    command: TimelineCommand;
    payload?: any;
}

@Injectable()
export class TimelineInteractionService {
    public onSelectPointingTime$: Observable<number>;
    public onSelectEventStatus$: Observable<ITimelineEventSegment>;
    public onCommand$: Observable<ITimelineCommandParam>;

    private changePointingTimeSource = new Subject<number>();
    private selectEventStatusSource = new Subject<ITimelineEventSegment>();
    private commandSource = new Subject<ITimelineCommandParam>();

    constructor() {
        this.onSelectPointingTime$ = this.changePointingTimeSource.asObservable();
        this.onSelectEventStatus$ = this.selectEventStatusSource.asObservable();
        this.onCommand$ = this.commandSource.asObservable();
    }
    sendSelectedPointingTime(pointingTime: number): void {
        this.changePointingTimeSource.next(pointingTime);
    }
    sendSelectedEventStatus(eventSegment: ITimelineEventSegment): void {
        this.selectEventStatusSource.next(eventSegment);
    }
    setZoomIn(): void {
        this.commandSource.next({
            command: TimelineCommand.zoomIn
        });
    }
    setZoomOut(): void {
        this.commandSource.next({
            command: TimelineCommand.zoomOut
        });
    }
    setPrev(): void {
        this.commandSource.next({
            command: TimelineCommand.prev
        });
    }
    setNext(): void {
        this.commandSource.next({
            command: TimelineCommand.next
        });
    }
    setNow(): void {
        this.commandSource.next({
            command: TimelineCommand.now
        });
    }
}

