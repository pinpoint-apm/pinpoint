import { Component, OnInit, OnDestroy, ChangeDetectionStrategy, ChangeDetectorRef, ViewChild, ComponentFactoryResolver, Injector } from '@angular/core';
import { Subject, Observable } from 'rxjs';
import { takeUntil, map, switchMap, tap, withLatestFrom } from 'rxjs/operators';

import { Actions } from 'app/shared/store';
import { StoreHelperService, NewUrlStateNotificationService, UrlRouteManagerService, AnalyticsService, DynamicPopupService, TRACKED_EVENT_LIST, MessageQueueService, MESSAGE_TO } from 'app/shared/services';
import { Timeline, ITimelineEventSegment, TimelineUIEvent } from './class';
import { TimelineComponent } from './timeline.component';
import { AgentTimelineDataService, IAgentTimeline } from './agent-timeline-data.service';
import { ServerErrorPopupContainerComponent } from 'app/core/components/server-error-popup';
import { UrlPathId } from 'app/shared/models';

@Component({
    selector: 'pp-agent-inspector-timeline-container',
    templateUrl: './agent-inspector-timeline-container.component.html',
    styleUrls: ['./agent-inspector-timeline-container.component.css'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class AgentInspectorTimelineContainerComponent implements OnInit, OnDestroy {
    @ViewChild(TimelineComponent)
    private timelineComponent: TimelineComponent;
    private unsubscribe: Subject<void> = new Subject();
    private agentId: string;

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
        private urlRouteManagerService: UrlRouteManagerService,
        private agentTimelineDataService: AgentTimelineDataService,
        private messageQueueService: MessageQueueService,
        private dynamicPopupService: DynamicPopupService,
        private analyticsService: AnalyticsService,
        private componentFactoryResolver: ComponentFactoryResolver,
        private injector: Injector
    ) {}

    ngOnInit() {
        this.connectStore();
        this.messageQueueService.receiveMessage(this.unsubscribe, MESSAGE_TO.TIMELINE_ZOOM_IN).subscribe(() => {
            this.analyticsService.trackEvent(TRACKED_EVENT_LIST.ZOOM_IN_TIMELINE);
            this.timelineComponent.zoomIn();
            this.updateTimelineData();
        });
        this.messageQueueService.receiveMessage(this.unsubscribe, MESSAGE_TO.TIMELINE_ZOOM_OUT).subscribe(() => {
            this.analyticsService.trackEvent(TRACKED_EVENT_LIST.ZOOM_OUT_TIMELINE);
            this.timelineComponent.zoomOut();
            this.updateTimelineData();
        });
        this.messageQueueService.receiveMessage(this.unsubscribe, MESSAGE_TO.TIMELINE_MOVE_PREV).subscribe(() => {
            this.analyticsService.trackEvent(TRACKED_EVENT_LIST.MOVE_TO_PREV_ON_TIMELINE);
            this.timelineComponent.movePrev();
            this.updateTimelineData();
        });
        this.messageQueueService.receiveMessage(this.unsubscribe, MESSAGE_TO.TIMELINE_MOVE_NEXT).subscribe(() => {
            this.analyticsService.trackEvent(TRACKED_EVENT_LIST.MOVE_TO_PREV_ON_TIMELINE);
            this.timelineComponent.moveNext();
            this.updateTimelineData();
        });
        this.messageQueueService.receiveMessage(this.unsubscribe, MESSAGE_TO.TIMELINE_MOVE_NOW).subscribe(() => {
            this.analyticsService.trackEvent(TRACKED_EVENT_LIST.MOVE_TO_NOW_ON_TIMELINE);
            this.timelineComponent.moveNow();
            this.updateTimelineData();
        });
        this.newUrlStateNotificationService.onUrlStateChange$.pipe(
            takeUntil(this.unsubscribe),
            tap((urlService: NewUrlStateNotificationService) => {
                if (urlService.isValueChanged(UrlPathId.AGENT_ID)) {
                    this.agentId = urlService.getPathValue(UrlPathId.AGENT_ID);
                }
            }),
            withLatestFrom(this.storeHelperService.getInspectorTimelineData(this.unsubscribe)),
            map(([urlService, storeState]: [NewUrlStateNotificationService, ITimelineInfo]) => {
                if ((urlService.isValueChanged(UrlPathId.PERIOD) || urlService.isValueChanged(UrlPathId.END_TIME)) || storeState.selectedTime === 0) {
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
            tap((timelineInfo: ITimelineInfo) => {
                this.timelineStartTime = timelineInfo.range[0];
                this.timelineEndTime = timelineInfo.range[1];
                this.selectionStartTime = timelineInfo.selectionRange[0];
                this.selectionEndTime = timelineInfo.selectionRange[1];
                this.pointingTime = timelineInfo.selectedTime;
            }),
            switchMap(({range}: {range: number[]}) => {
                return this.agentTimelineDataService.getData(this.agentId, range);
            })
        ).subscribe((response: IAgentTimeline) => {
            this.timelineData = response;
            this.changeDetector.detectChanges();
        }, (error: IServerErrorFormat) => {
            this.dynamicPopupService.openPopup({
                data: {
                    title: 'Error',
                    contents: error
                },
                component: ServerErrorPopupContainerComponent,
                onCloseCallback: () => {
                    this.urlRouteManagerService.reload();
                }
            }, {
                resolver: this.componentFactoryResolver,
                injector: this.injector
            });
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
    updateTimelineData(): void {
        const range = this.timelineComponent.getTimelineRange();

        this.agentTimelineDataService.getData(this.agentId, range).subscribe((response: IAgentTimeline) => {
            this.timelineComponent.updateData(response);
        });
    }
    onSelectEventStatus($eventObj: ITimelineEventSegment): void {
        this.messageQueueService.sendMessage({
            to: MESSAGE_TO.TIMELINE_SELECTED_EVENT_STATUS,
            param: [$eventObj]
        });
    }
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
