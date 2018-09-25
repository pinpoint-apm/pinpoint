import { Component, OnInit, OnDestroy, ChangeDetectionStrategy, ChangeDetectorRef, ViewChild } from '@angular/core';
import { Subject, Observable } from 'rxjs';
import { takeUntil, withLatestFrom } from 'rxjs/operators';

import { UrlPathId } from 'app/shared/models';
import { Actions } from 'app/shared/store';
import { StoreHelperService, NewUrlStateNotificationService, AnalyticsService, TRACKED_EVENT_LIST } from 'app/shared/services';
import { Timeline, ITimelineEventSegment, TimelineUIEvent } from './class';
import { TimelineComponent } from './timeline.component';
import { TimelineInteractionService, ITimelineCommandParam, TimelineCommand } from './timeline-interaction.service';
import { IAgentTimeline, IRetrieveTime } from './agent-timeline-data.service';

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
        ).subscribe(([urlService, savedTimelineData]: [NewUrlStateNotificationService, ITimelineInfo]) => {
            /*
                if ( application.changed or period.changed ) {
                    url 값 사용
                } else {
                    store에 저장된 값 있나?
                    - 있다면 사용
                    - 없다면 URL 값 사용
                }
            */
            const selectionStartTime = urlService.getStartTimeToNumber();
            const selectionEndTime = urlService.getEndTimeToNumber();
            const range = this.calcuRetrieveTime(selectionStartTime, selectionEndTime);
            let timelineInfo: ITimelineInfo = {
                range: [range.start, range.end],
                selectedTime: selectionEndTime,
                selectionRange: [selectionStartTime, selectionEndTime]
            };
            if (urlService.isChanged(UrlPathId.APPLICATION) === false && urlService.isChanged(UrlPathId.PERIOD) === false) {
                if (savedTimelineData.selectedTime !== 0) {
                    timelineInfo = savedTimelineData;
                }
            }
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
            this.storeHelperService.dispatch(new Actions.UpdateTimelineData(timelineInfo));
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
    calcuRetrieveTime(startTime: number, endTime: number ): IRetrieveTime {
        const allowedMaxRagne = Timeline.MAX_TIME_RANGE;
        const timeGap = endTime - startTime;
        if ( timeGap > allowedMaxRagne  ) {
            return {
                start: endTime - allowedMaxRagne,
                end: endTime
            };
        } else {
            const calcuStart = timeGap * 3;
            return {
                start: endTime - (calcuStart > allowedMaxRagne ? allowedMaxRagne : calcuStart),
                end:  endTime
            };
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
