import { Component, OnInit, OnDestroy, ViewChild, ComponentFactoryResolver, Injector } from '@angular/core';
import { Subject, Observable } from 'rxjs';
import { map, switchMap, tap, filter, withLatestFrom } from 'rxjs/operators';

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
})
export class AgentInspectorTimelineContainerComponent implements OnInit, OnDestroy {
    @ViewChild(TimelineComponent)
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
    timelineInfoFromUrl$: Observable<ITimelineInfo>;
    timelineInfoFromStore$: Observable<ITimelineInfo>;

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

        this.timelineInfoFromStore$ = this.storeHelperService.getInspectorTimelineData(this.unsubscribe);
        this.timelineInfoFromUrl$ = this.storeHelperService.getRange(this.unsubscribe).pipe(
            filter((range: number[]) => !!range),
            map(([from, to]: number[]) => {
                return {
                    range: this.calcuRetrieveTime(from, to),
                    selectedTime: to,
                    selectionRange: [from, to]
                } as ITimelineInfo;
            }),
            tap((timelineInfo: ITimelineInfo) => {
                const urlService = this.newUrlStateNotificationService;

                if (urlService.isRealTimeMode() || urlService.isValueChanged(UrlPathId.PERIOD) || urlService.isValueChanged(UrlPathId.END_TIME)) {
                    this.storeHelperService.dispatch(new Actions.UpdateTimelineData(timelineInfo));
                }
            })
        );

        this.timelineInfoFromUrl$.pipe(
            withLatestFrom(this.timelineInfoFromStore$),
            map(([timelineInfoFromUrl, timelineInfoFromStore]: ITimelineInfo[]) => {
                const urlService = this.newUrlStateNotificationService;
                const agentId = urlService.getPathValue(UrlPathId.AGENT_ID);
                const shouldUseInfoFromUrl = urlService.isRealTimeMode() || urlService.isValueChanged(UrlPathId.PERIOD) || urlService.isValueChanged(UrlPathId.END_TIME);
                const { range, selectionRange, selectedTime } = shouldUseInfoFromUrl ? timelineInfoFromUrl : timelineInfoFromStore;

                this.timelineStartTime = range[0];
                this.timelineEndTime = range[1];
                this.selectionStartTime = selectionRange[0];
                this.selectionEndTime = selectionRange[1];
                this.pointingTime = selectedTime;

                return { agentId, range };
            }),
            switchMap(({agentId, range}: {agentId: string, range: number[]}) => {
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
        this.dateFormat$ = this.storeHelperService.getDateFormatArray(this.unsubscribe, 0, 5, 6);
    }
    calcuRetrieveTime(startTime: number, endTime: number): number[] {
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
        const agentId = this.newUrlStateNotificationService.getPathValue(UrlPathId.AGENT_ID);

        this.agentTimelineDataService.getData(agentId, range).subscribe((response: IAgentTimeline) => {
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
