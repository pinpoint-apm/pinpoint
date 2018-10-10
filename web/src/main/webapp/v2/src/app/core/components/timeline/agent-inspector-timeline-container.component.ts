import { Component, OnInit, OnDestroy, ChangeDetectionStrategy, ChangeDetectorRef, ViewChild } from '@angular/core';
import { Subject, Observable } from 'rxjs';
import { takeUntil, withLatestFrom } from 'rxjs/operators';

import { UrlPathId } from 'app/shared/models';
import { Actions } from 'app/shared/store';
import { StoreHelperService, NewUrlStateNotificationService, UrlRouteManagerService, AnalyticsService, DynamicPopupService, TRACKED_EVENT_LIST } from 'app/shared/services';
import { Timeline, ITimelineEventSegment, TimelineUIEvent } from './class';
import { TimelineComponent } from './timeline.component';
import { TimelineInteractionService, ITimelineCommandParam, TimelineCommand } from './timeline-interaction.service';
import { AgentTimelineDataService, IAgentTimeline, IRetrieveTime } from './agent-timeline-data.service';
import { ServerErrorPopupContainerComponent } from 'app/core/components/server-error-popup';
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
    private agentId = '';
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
        private timelineInteractionService: TimelineInteractionService,
        private dynamicPopupService: DynamicPopupService,
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
            this.updateTimelineData();
        });
        this.newUrlStateNotificationService.onUrlStateChange$.pipe(
            takeUntil(this.unsubscribe),
            withLatestFrom(this.storeHelperService.getInspectorTimelineData(this.unsubscribe))
        ).subscribe(([urlService, savedTimelineData]: [NewUrlStateNotificationService, ITimelineInfo]) => {
            this.agentId = this.newUrlStateNotificationService.getPathValue(UrlPathId.AGENT_ID);
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
            this.agentTimelineDataService.getData(this.agentId, {
                start: timelineInfo.range[0],
                end: timelineInfo.range[1]
            }).subscribe((response: IAgentTimeline) => {
                this.timelineStartTime = timelineInfo.range[0];
                this.timelineEndTime = timelineInfo.range[1];
                this.selectionStartTime = timelineInfo.selectionRange[0];
                this.selectionEndTime = timelineInfo.selectionRange[1];
                this.pointingTime = timelineInfo.selectedTime;
                this.timelineData = response;
                this.storeHelperService.dispatch(new Actions.UpdateTimelineData(timelineInfo));
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
                });
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
    updateTimelineData(): void {
        const range = this.timelineComponent.getTimelineRange();
        this.agentTimelineDataService.getData(this.agentId, {
            start: range[0],
            end: range[1]
        }).subscribe((response: IAgentTimeline) => {
            this.timelineComponent.updateData(response);
        });
    }
    onSelectEventStatus($eventObj: ITimelineEventSegment): void {
        this.timelineInteractionService.sendSelectedEventStatus($eventObj);
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
