import { Component, OnInit, OnDestroy, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { Observable, Subject, merge } from 'rxjs';
import { filter, tap, map, switchMap, takeUntil, withLatestFrom, skip } from 'rxjs/operators';

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
    private prevEndTime: number;

    dataRequestSuccess: boolean;
    urlApplicationName: string;
    agentData: IServerAndAgentData;
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
        this.connectStore();
    }

    ngOnDestroy() {
        this.unsubscribe.next();
        this.unsubscribe.complete();
    }

    private connectStore(): void {
        this.timezone$ = this.storeHelperService.getTimezone(this.unsubscribe);
        this.dateFormat$ = this.storeHelperService.getDateFormat(this.unsubscribe, 1);
        merge(
            this.newUrlStateNotificationService.onUrlStateChange$.pipe(
                takeUntil(this.unsubscribe),
                tap((urlService: NewUrlStateNotificationService) => {
                    this.urlApplicationName = urlService.getPathValue(UrlPathId.APPLICATION).getApplicationName();
                }),
                withLatestFrom(this.storeHelperService.getInspectorTimelineSelectedTime(this.unsubscribe)),
                map(([urlService, storeState]: [NewUrlStateNotificationService, number]) => {
                    return urlService.isPathChanged(UrlPathId.PERIOD) || storeState === 0 ? urlService.getEndTimeToNumber()
                        : storeState;
                })
            ),
            this.storeHelperService.getInspectorTimelineSelectedTime(this.unsubscribe).pipe(skip(1))
        ).pipe(
            tap(() => {
                this.showLoading = true;
                this.changeDetectorRef.detectChanges();
            }),
            switchMap((endTime: number) => {
                this.prevEndTime = endTime;
                return this.agentInfoDataService.getData(endTime);
            }),
            filter((agentData: IServerAndAgentData) => {
                return !!(agentData && agentData.applicationName);
            })
        ).subscribe((agentData: IServerAndAgentData) => {
            this.agentData = agentData;
            this.dataRequestSuccess = true;
            this.completed();
        }, (error: IServerErrorFormat) => {
            this.dataRequestSuccess = false;
            this.completed();
        });
    }
    private completed(): void {
        this.showLoading = false;
        this.changeDetectorRef.detectChanges();
    }
    onRequestAgain(): void {
        this.showLoading = true;
        this.agentInfoDataService.getData(this.prevEndTime).subscribe((agentData: IServerAndAgentData) => {
            this.agentData = agentData;
            this.dataRequestSuccess = true;
            this.completed();
        }, (error: IServerErrorFormat) => {
            this.dataRequestSuccess = false;
            this.completed();
        });
    }
    onClickApplicationNameIssue({data, coord}: {data: {[key: string]: string}, coord: ICoordinate}): void {
        this.dynamicPopupService.openPopup({
            data,
            coord,
            component: ApplicationNameIssuePopupContainerComponent
        });
    }
}
