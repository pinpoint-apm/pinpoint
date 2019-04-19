import { Component, OnInit, OnDestroy, ComponentFactoryResolver, Injector } from '@angular/core';
import { Observable, Subject, combineLatest } from 'rxjs';
import { filter, tap, map, switchMap, takeUntil } from 'rxjs/operators';

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
})
export class AgentInfoContainerComponent implements OnInit, OnDestroy {
    private unsubscribe: Subject<void> = new Subject();
    private selectedTime$: Observable<number>;
    private urlAgentId$: Observable<string>;
    private lastRequestParam: [string, number];
    dataRequestSuccess: boolean;
    urlApplicationName$: Observable<string>;
    agentData: IServerAndAgentData;
    timezone$: Observable<string>;
    dateFormat$: Observable<string>;
    showLoading = true;

    constructor(
        private newUrlStateNotificationService: NewUrlStateNotificationService,
        private storeHelperService: StoreHelperService,
        private agentInfoDataService: AgentInfoDataService,
        private dynamicPopupService: DynamicPopupService,
        private componentFactoryResolver: ComponentFactoryResolver,
        private injector: Injector
    ) {}

    ngOnInit() {
        this.urlAgentId$ = this.newUrlStateNotificationService.onUrlStateChange$.pipe(
            takeUntil(this.unsubscribe),
            filter((urlService: NewUrlStateNotificationService) => {
                // * Route의 sub root가 바뀔 때(ex: realtime <=> non-realtime) 컴포넌트가 destroy되었다가 다시 init되는데, 이걸 감지할 다른방법이 있을지..
                // * 이때, agentId는 바뀐게 아니라서 해당 스트림이 emit을 하지않고, 밑에 combineLatest가 동작을 안함.
                // * 이걸 감지하기 위해서 임시로 agentData 존재여부를 체크하는 조건을 추가함.
                // ! sub root change를 detect할 수 있는 방법이 있으면 리팩토링하기.
                return !this.agentData || urlService.isValueChanged(UrlPathId.AGENT_ID);
            }),
            map((urlService: NewUrlStateNotificationService) => {
                return urlService.getPathValue(UrlPathId.AGENT_ID);
            })
        );
        this.urlApplicationName$ = this.newUrlStateNotificationService.onUrlStateChange$.pipe(
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
        combineLatest(
            this.urlAgentId$,
            this.selectedTime$
        ).pipe(
            tap(() => {
                this.showLoading = true;
            }),
            switchMap(([agentId, endTime]: [string, number]) => {
                this.lastRequestParam = [agentId, endTime];
                return this.agentInfoDataService.getData(agentId, endTime);
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
    }
    onRequestAgain(): void {
        const [agentId, endTime] = this.lastRequestParam;
        this.showLoading = true;
        this.agentInfoDataService.getData(agentId, endTime).subscribe((agentData: IServerAndAgentData) => {
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
        }, {
            resolver: this.componentFactoryResolver,
            injector: this.injector
        });
    }
}
