import { Component, OnInit, OnDestroy, ChangeDetectionStrategy, ChangeDetectorRef, ComponentFactoryResolver, Injector } from '@angular/core';
import { Observable, Subject } from 'rxjs';
import { switchMap } from 'rxjs/operators';

import { StoreHelperService, DynamicPopupService, MessageQueueService, MESSAGE_TO } from 'app/shared/services';
import { ITimelineEventSegment } from 'app/core/components/timeline/class/timeline-data.class';
import { AgentEventsDataService, IEventStatus } from './agent-events-data.service';
import { ServerErrorPopupContainerComponent } from 'app/core/components/server-error-popup/server-error-popup-container.component';

@Component({
    selector: 'pp-agent-event-view-container',
    templateUrl: './agent-event-view-container.component.html',
    styleUrls: ['./agent-event-view-container.component.css'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class AgentEventViewContainerComponent implements OnInit, OnDestroy {
    private unsubscribe = new Subject<void>();

    viewComponent = false;
    eventData: IEventStatus[];
    timezone$: Observable<string>;
    dateFormat$: Observable<string>;

    constructor(
        private changeDetectorRef: ChangeDetectorRef,
        private storeHelperService: StoreHelperService,
        private messageQueueService: MessageQueueService,
        private agentEventsDataService: AgentEventsDataService,
        private dynamicPopupService: DynamicPopupService,
        private componentFactoryResolver: ComponentFactoryResolver,
        private injector: Injector
    ) {}

    ngOnInit() {
        this.connectStore();
    }

    private connectStore(): void {
        this.timezone$ = this.storeHelperService.getTimezone(this.unsubscribe);
        this.dateFormat$ = this.storeHelperService.getDateFormat(this.unsubscribe, 1);
        this.messageQueueService.receiveMessage(this.unsubscribe, MESSAGE_TO.TIMELINE_SELECTED_EVENT_STATUS).pipe(
            switchMap((eventSegment: ITimelineEventSegment) => {
                return this.agentEventsDataService.getData(eventSegment.startTimestamp, eventSegment.endTimestamp);
            })
        ).subscribe((response: IEventStatus[]) => {
            this.eventData = response;
            this.viewComponent = true;
            this.changeDetectorRef.detectChanges();
        }, (error: IServerErrorFormat) => {
            this.dynamicPopupService.openPopup({
                data: {
                    title: 'Error',
                    contents: error
                },
                component: ServerErrorPopupContainerComponent
            }, {
                resolver: this.componentFactoryResolver,
                injector: this.injector
            });
        });
    }

    onClose(): void {
        this.viewComponent = false;
        this.changeDetectorRef.detectChanges();
    }

    ngOnDestroy() {
        this.unsubscribe.next();
        this.unsubscribe.complete();
    }
}
