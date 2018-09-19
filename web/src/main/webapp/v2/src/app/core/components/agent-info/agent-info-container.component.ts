import { Component, OnInit, OnDestroy, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { Observable, Subject, combineLatest } from 'rxjs';
import { takeUntil, filter, tap, map, switchMap } from 'rxjs/operators';

import { UrlPathId } from 'app/shared/models';
import {
    StoreHelperService,
    NewUrlStateNotificationService,
    DynamicPopupService
} from 'app/shared/services';
import { ApplicationNameIssuePopupContainerComponent } from 'app/core/components/application-name-issue-popup/application-name-issue-popup-container.component';
import { AgentInfoDataService } from './agent-info-data.service';

@Component({
    selector: 'pp-agent-info-container',
    templateUrl: './agent-info-container.component.html',
    styleUrls: ['./agent-info-container.component.css'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class AgentInfoContainerComponent implements OnInit, OnDestroy {
    private unsubscribe: Subject<void> = new Subject();
    private selectedTime$: Observable<number>;
    private urlAgentId$: Observable<string>;
    urlApplicationName$: Observable<string>;
    agentData$: Observable<IServerAndAgentData>;
    timezone$: Observable<string>;
    dateFormat$: Observable<string>;
    showLoading = true;

    constructor(
        private changeDetectorRef: ChangeDetectorRef,
        private newUrlStateNotificationService: NewUrlStateNotificationService,
        private storeHelperService: StoreHelperService,
        private agentInfoDataService: AgentInfoDataService,
        private dynamicPopupService: DynamicPopupService,
    ) {}

    ngOnInit() {
        this.urlAgentId$ = this.newUrlStateNotificationService.onUrlStateChange$.pipe(
            takeUntil(this.unsubscribe),
            map((urlService: NewUrlStateNotificationService) => {
                return urlService.getPathValue(UrlPathId.AGENT_ID);
            })
        );
        this.urlApplicationName$ = this.newUrlStateNotificationService.onUrlStateChange$.pipe(
            takeUntil(this.unsubscribe),
            map((urlService: NewUrlStateNotificationService) => {
                return urlService.getPathValue(UrlPathId.APPLICATION).getApplicationName();
            })
        );
        this.connectStore();
    }

    ngOnDestroy() {
        this.unsubscribe.next();
        this.unsubscribe.complete();
    }

    private connectStore(): void {
        this.timezone$ = this.storeHelperService.getTimezone(this.unsubscribe);
        this.dateFormat$ = this.storeHelperService.getDateFormat(this.unsubscribe, 1);
        this.selectedTime$ = this.storeHelperService.getInspectorTimelineSelectedTime(this.unsubscribe);
        this.agentData$ = combineLatest(
            this.urlAgentId$,
            this.selectedTime$
        ).pipe(
            tap(() => {
                this.showLoading = true;
                this.changeDetectorRef.detectChanges();
            }),
            switchMap((data: [string, number]) => {
                return this.agentInfoDataService.getData(data[0], data[1]);
            }),
            filter((agentData: IServerAndAgentData) => {
                return !!(agentData && agentData.applicationName);
            }),
            tap(() => {
                this.showLoading = false;
                this.changeDetectorRef.detectChanges();
            })
        );
    }

    onClickApplicationNameIssue({data, coord}: {data: {[key: string]: string}, coord: ICoordinate}): void {
        this.dynamicPopupService.openPopup({
            data,
            coord,
            component: ApplicationNameIssuePopupContainerComponent
        });
    }
}
