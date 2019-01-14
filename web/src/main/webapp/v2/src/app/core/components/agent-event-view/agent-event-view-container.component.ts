import { Component, OnInit, OnDestroy, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { Observable, Subject } from 'rxjs';
import { takeUntil, switchMap } from 'rxjs/operators';

import { StoreHelperService, DynamicPopupService } from 'app/shared/services';
import { ITimelineEventSegment } from 'app/core/components/timeline/class/timeline-data.class';
import { TimelineInteractionService } from 'app/core/components/timeline/timeline-interaction.service';
import { AgentEventsDataService, IEventStatus } from './agent-events-data.service';
import { ServerErrorPopupContainerComponent } from 'app/core/components/server-error-popup';

@Component({
    selector: 'pp-agent-event-view-container',
    templateUrl: './agent-event-view-container.component.html',
    styleUrls: ['./agent-event-view-container.component.css'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class AgentEventViewContainerComponent implements OnInit, OnDestroy {
    private unsubscribe: Subject<void> = new Subject();
    viewComponent = false;
    eventData: IEventStatus[];
    timezone$: Observable<string>;
    dateFormat$: Observable<string>;
    constructor(
        private changeDetectorRef: ChangeDetectorRef,
        private storeHelperService: StoreHelperService,
        private timelineInteractionService: TimelineInteractionService,
        private agentEventsDataService: AgentEventsDataService,
        private dynamicPopupService: DynamicPopupService
    ) {}
    ngOnInit() {
        this.connectStore();
    }
    private connectStore(): void {
        this.timezone$ = this.storeHelperService.getTimezone(this.unsubscribe);
        this.dateFormat$ = this.storeHelperService.getDateFormat(this.unsubscribe, 1);
        this.timelineInteractionService.onSelectEventStatus$.pipe(
            takeUntil(this.unsubscribe),
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
