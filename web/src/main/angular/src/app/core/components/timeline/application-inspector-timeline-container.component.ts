import { Component, OnInit, OnDestroy, ViewChild } from '@angular/core';
import { Subject, Observable } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

import { StoreHelperService, AnalyticsService, TRACKED_EVENT_LIST, MessageQueueService, MESSAGE_TO } from 'app/shared/services';
import { ITimelineEventSegment, TimelineUIEvent } from './class';
import { TimelineComponent } from './timeline.component';
import { IAgentTimeline } from './agent-timeline-data.service';
import { InspectorPageService, ISourceForTimeline } from 'app/routes/inspector-page/inspector-page.service';

@Component({
    selector: 'pp-application-inspector-timeline-container',
    templateUrl: './application-inspector-timeline-container.component.html',
    styleUrls: ['./application-inspector-timeline-container.component.css'],
})
export class ApplicationInspectorTimelineContainerComponent implements OnInit, OnDestroy {
    @ViewChild(TimelineComponent, { static: true })
    private timelineComponent: TimelineComponent;
    private unsubscribe = new Subject<void>();

    timelineStartTime: number;
    timelineEndTime: number;
    selectionStartTime: number;
    selectionEndTime: number;
    pointingTime: number;
    timelineData: IAgentTimeline;
    timezone$: Observable<string>;
    dateFormat$: Observable<string[]>;
    timelineInfoFromUrl$: Observable<ITimelineInfo>;
    timelineInfoFromStore$: Observable<ITimelineInfo>;

    constructor(
        private storeHelperService: StoreHelperService,
        private messageQueueService: MessageQueueService,
        private analyticsService: AnalyticsService,
        private inspectorPageService: InspectorPageService,
    ) {}

    ngOnInit() {
        this.connectStore();
        this.messageQueueService.receiveMessage(this.unsubscribe, MESSAGE_TO.TIMELINE_ZOOM_IN).subscribe(() => {
            this.analyticsService.trackEvent(TRACKED_EVENT_LIST.ZOOM_IN_TIMELINE);
            this.timelineComponent.zoomIn();
        });
        this.messageQueueService.receiveMessage(this.unsubscribe, MESSAGE_TO.TIMELINE_ZOOM_OUT).subscribe(() => {
            this.analyticsService.trackEvent(TRACKED_EVENT_LIST.ZOOM_OUT_TIMELINE);
            this.timelineComponent.zoomOut();
        });
        this.messageQueueService.receiveMessage(this.unsubscribe, MESSAGE_TO.TIMELINE_MOVE_PREV).subscribe(() => {
            this.analyticsService.trackEvent(TRACKED_EVENT_LIST.MOVE_TO_PREV_ON_TIMELINE);
            this.timelineComponent.movePrev();
        });
        this.messageQueueService.receiveMessage(this.unsubscribe, MESSAGE_TO.TIMELINE_MOVE_NEXT).subscribe(() => {
            this.analyticsService.trackEvent(TRACKED_EVENT_LIST.MOVE_TO_PREV_ON_TIMELINE);
            this.timelineComponent.moveNext();
        });
        this.messageQueueService.receiveMessage(this.unsubscribe, MESSAGE_TO.TIMELINE_MOVE_NOW).subscribe(() => {
            this.analyticsService.trackEvent(TRACKED_EVENT_LIST.MOVE_TO_NOW_ON_TIMELINE);
            this.timelineComponent.moveNow();
        });

        this.inspectorPageService.sourceForTimeline$.pipe(
            takeUntil(this.unsubscribe),
        ).subscribe(({timelineInfo: {range, selectionRange, selectedTime}}: ISourceForTimeline) => {
            this.timelineStartTime = range[0];
            this.timelineEndTime = range[1];
            this.selectionStartTime = selectionRange[0];
            this.selectionEndTime = selectionRange[1];
            this.pointingTime = selectedTime;
            this.timelineData = {
                'agentStatusTimeline': {
                    'timelineSegments': [
                        {
                            'startTimestamp': range[0],
                            'endTimestamp': range[1],
                            'value': 'EMPTY'
                        }
                    ],
                    'includeWarning': false
                },
                'agentEventTimeline': {
                    'timelineSegments': []
                }
            };
        });
    }
    ngOnDestroy() {
        this.unsubscribe.next();
        this.unsubscribe.complete();
    }
    private connectStore(): void {
        this.timezone$ = this.storeHelperService.getTimezone(this.unsubscribe);
        this.dateFormat$ = this.storeHelperService.getDateFormatArray(this.unsubscribe, 0, 6, 7);
    }
    onSelectEventStatus($eventObj: ITimelineEventSegment): void {}
    onChangeTimelineUIEvent(event: TimelineUIEvent): void {
        if (event.changedSelectedTime) {
            this.analyticsService.trackEvent(TRACKED_EVENT_LIST.CHANGE_POINTING_TIME_ON_TIMELINE);
        }
        if (event.changedSelectionRange) {
            this.analyticsService.trackEvent(TRACKED_EVENT_LIST.CHANGE_SELECTION_RANGE_ON_TIMELINE);
        }
        this.inspectorPageService.updateTimelineData(event.data);
    }
}
