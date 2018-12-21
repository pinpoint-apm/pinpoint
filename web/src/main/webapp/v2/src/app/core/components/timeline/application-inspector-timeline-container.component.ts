import { Component, OnInit, OnDestroy, ChangeDetectionStrategy, ChangeDetectorRef, ViewChild } from '@angular/core';
import { Subject, Observable } from 'rxjs';
import { takeUntil, withLatestFrom, map } from 'rxjs/operators';

import { Actions } from 'app/shared/store';
import { StoreHelperService, NewUrlStateNotificationService, AnalyticsService, TRACKED_EVENT_LIST } from 'app/shared/services';
import { Timeline, ITimelineEventSegment, TimelineUIEvent } from './class';
import { TimelineComponent } from './timeline.component';
import { TimelineInteractionService, ITimelineCommandParam, TimelineCommand } from './timeline-interaction.service';
import { IAgentTimeline } from './agent-timeline-data.service';
import { UrlPathId } from 'app/shared/models';

@Component({
    selector: 'pp-application-inspector-timeline-container',
    templateUrl: './application-inspector-timeline-container.component.html',
    styleUrls: ['./application-inspector-timeline-container.component.css'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class ApplicationInspectorTimelineContainerComponent implements OnInit, OnDestroy {
    @ViewChild(TimelineComponent)
    private timelineComponent: TimelineComponent;
    private unsubscribe: Subject<null> = new Subject();
    timelineStartTime: number;
    timelineEndTime: number;
    selectionStartTime: number;
    selectionEndTime: number;
    pointingTime: number;
    timelineData: IAgentTimeline;
    timezone$: Observable<string>;
    dateFormat$: Observable<string[]>;

    constructor(
        private changeDetector: ChangeDetectorRef,
        private storeHelperService: StoreHelperService,
        private newUrlStateNotificationService: NewUrlStateNotificationService,
        private timelineInteractionService: TimelineInteractionService,
        private analyticsService: AnalyticsService,
    ) {}

    ngOnInit() {
        this.connectStore();
        this.timelineInteractionService.onCommand$.pipe(
            takeUntil(this.unsubscribe)
        ).subscribe((param: ITimelineCommandParam) => {
            switch (param.command) {
                case TimelineCommand.zoomIn:
                    this.analyticsService.trackEvent(TRACKED_EVENT_LIST.ZOOM_IN_TIMELINE);
                    this.timelineComponent.zoomIn();
                    break;
                case TimelineCommand.zoomOut:
                    this.analyticsService.trackEvent(TRACKED_EVENT_LIST.ZOOM_OUT_TIMELINE);
                    this.timelineComponent.zoomOut();
                    break;
                case TimelineCommand.prev:
                    this.analyticsService.trackEvent(TRACKED_EVENT_LIST.MOVE_TO_PREV_ON_TIMELINE);
                    this.timelineComponent.movePrev();
                    break;
                case TimelineCommand.next:
                    this.analyticsService.trackEvent(TRACKED_EVENT_LIST.MOVE_TO_NEXT_ON_TIMELINE);
                    this.timelineComponent.moveNext();
                    break;
                case TimelineCommand.now:
                    this.analyticsService.trackEvent(TRACKED_EVENT_LIST.MOVE_TO_NOW_ON_TIMELINE);
                    this.timelineComponent.moveNow();
                    break;
            }
        });
        this.newUrlStateNotificationService.onUrlStateChange$.pipe(
            takeUntil(this.unsubscribe),
            withLatestFrom(this.storeHelperService.getInspectorTimelineData(this.unsubscribe)),
            map(([urlService, storeState]: [NewUrlStateNotificationService, ITimelineInfo]) => {
                if (urlService.isPathChanged(UrlPathId.PERIOD) || urlService.isPathChanged(UrlPathId.END_TIME)) {
                    const selectionStartTime = urlService.getStartTimeToNumber();
                    const selectionEndTime = urlService.getEndTimeToNumber();
                    const [start, end] = this.calcuRetrieveTime(selectionStartTime, selectionEndTime);
                    const timelineInfo: ITimelineInfo = {
                        range: [start, end],
                        selectedTime: selectionEndTime,
                        selectionRange: [selectionStartTime, selectionEndTime]
                    };

                    this.storeHelperService.dispatch(new Actions.UpdateTimelineData(timelineInfo));
                    return timelineInfo;
                } else {
                    return storeState;
                }
            }),
        ).subscribe((timelineInfo: ITimelineInfo) => {
            this.timelineStartTime = timelineInfo.range[0];
            this.timelineEndTime = timelineInfo.range[1];
            this.selectionStartTime = timelineInfo.selectionRange[0];
            this.selectionEndTime = timelineInfo.selectionRange[1];
            this.pointingTime = timelineInfo.selectedTime;
            this.timelineData = {
                'agentStatusTimeline': {
                    'timelineSegments': [
                        {
                            'startTimestamp': timelineInfo.range[0],
                            'endTimestamp': timelineInfo.range[1],
                            'value': 'EMPTY'
                        }
                    ],
                    'includeWarning': false
                },
                'agentEventTimeline': {
                    'timelineSegments': []
                }
            };

            this.changeDetector.detectChanges();
        });
    }
    ngOnDestroy() {
        this.unsubscribe.next();
        this.unsubscribe.complete();
    }
    private connectStore(): void {
        this.timezone$ = this.storeHelperService.getTimezone(this.unsubscribe);
        this.dateFormat$ = this.storeHelperService.getDateFormatArray(this.unsubscribe, 0, 5, 6);
    }
    calcuRetrieveTime(startTime: number, endTime: number ): number[] {
        const allowedMaxRagne = Timeline.MAX_TIME_RANGE;
        const timeGap = endTime - startTime;

        if (timeGap > allowedMaxRagne) {
            return [endTime - allowedMaxRagne, endTime];
        } else {
            const calcuStart = timeGap * 3;

            return [endTime - (calcuStart > allowedMaxRagne ? allowedMaxRagne : calcuStart), endTime];
        }
    }
    onSelectEventStatus($eventObj: ITimelineEventSegment): void {}
    onChangeTimelineUIEvent(event: TimelineUIEvent): void {
        if (event.changedSelectedTime) {
            this.analyticsService.trackEvent(TRACKED_EVENT_LIST.CHANGE_POINTING_TIME_ON_TIMELINE);
        }
        if (event.changedSelectionRange) {
            this.analyticsService.trackEvent(TRACKED_EVENT_LIST.CHANGE_SELECTION_RANGE_ON_TIMELINE);
        }
        this.storeHelperService.dispatch(new Actions.UpdateTimelineData(event.data));
    }
}
