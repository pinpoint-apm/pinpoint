import { Component, OnInit, OnDestroy, ViewChild, ComponentFactoryResolver, Injector } from '@angular/core';
import { Subject, Observable } from 'rxjs';
import { map, switchMap, takeUntil } from 'rxjs/operators';

import { StoreHelperService, NewUrlStateNotificationService, UrlRouteManagerService, AnalyticsService, DynamicPopupService, TRACKED_EVENT_LIST, MessageQueueService, MESSAGE_TO } from 'app/shared/services';
import { ITimelineEventSegment, TimelineUIEvent } from './class';
import { TimelineComponent } from './timeline.component';
import { AgentTimelineDataService, IAgentTimeline } from './agent-timeline-data.service';
import { ServerErrorPopupContainerComponent } from 'app/core/components/server-error-popup/server-error-popup-container.component';
import { UrlPathId } from 'app/shared/models';
import { InspectorPageService, ISourceForTimeline } from 'app/routes/inspector-page/inspector-page.service';

@Component({
    selector: 'pp-agent-inspector-timeline-container',
    templateUrl: './agent-inspector-timeline-container.component.html',
    styleUrls: ['./agent-inspector-timeline-container.component.css'],
})
export class AgentInspectorTimelineContainerComponent implements OnInit, OnDestroy {
    @ViewChild(TimelineComponent, { static: true })
    private timelineComponent: TimelineComponent;
    private unsubscribe: Subject<void> = new Subject();

    timelineStartTime: number;
    timelineEndTime: number;
    selectionStartTime: number;
    selectionEndTime: number;
    pointingTime: number;
    timelineData: IAgentTimeline;
    timezone$: Observable<string>;
    dateFormat$: Observable<string[]>;

    constructor(
        private storeHelperService: StoreHelperService,
        private newUrlStateNotificationService: NewUrlStateNotificationService,
        private urlRouteManagerService: UrlRouteManagerService,
        private agentTimelineDataService: AgentTimelineDataService,
        private messageQueueService: MessageQueueService,
        private dynamicPopupService: DynamicPopupService,
        private analyticsService: AnalyticsService,
        private componentFactoryResolver: ComponentFactoryResolver,
        private injector: Injector,
        private inspectorPageService: InspectorPageService,
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

        this.inspectorPageService.sourceForTimeline$.pipe(
            takeUntil(this.unsubscribe),
            map(({timelineInfo, agentId}: ISourceForTimeline) => {
                const {range, selectionRange, selectedTime} = timelineInfo;

                this.timelineStartTime = range[0];
                this.timelineEndTime = range[1];
                this.selectionStartTime = selectionRange[0];
                this.selectionEndTime = selectionRange[1];
                this.pointingTime = selectedTime;

                return {range, agentId};
            }),
            switchMap(({range, agentId}: {range: number[], agentId: string}) => {
                return this.agentTimelineDataService.getData(agentId, range);
            })
        ).subscribe((response: IAgentTimeline) => {
            this.timelineData = response;
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
        this.dateFormat$ = this.storeHelperService.getDateFormatArray(this.unsubscribe, 0, 6, 7);
    }
    updateTimelineData(): void {
        const range = this.timelineComponent.getTimelineRange();
        const agentId = this.newUrlStateNotificationService.getPathValue(UrlPathId.AGENT_ID);

        this.agentTimelineDataService.getData(agentId, range).subscribe((response: IAgentTimeline) => {
            this.timelineComponent.updateData(response);
        });
    }
    onSelectEventStatus($eventObj: ITimelineEventSegment): void {
        this.messageQueueService.sendMessage({
            to: MESSAGE_TO.TIMELINE_SELECTED_EVENT_STATUS,
            param: $eventObj
        });
    }
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
