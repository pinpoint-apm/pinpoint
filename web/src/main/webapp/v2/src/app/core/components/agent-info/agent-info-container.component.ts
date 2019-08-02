import { Component, OnInit, OnDestroy, ComponentFactoryResolver, Injector } from '@angular/core';
import { Observable, Subject } from 'rxjs';
import { filter, tap, map, switchMap, takeUntil } from 'rxjs/operators';

import { UrlPathId } from 'app/shared/models';
import {
    StoreHelperService,
    NewUrlStateNotificationService,
    DynamicPopupService
} from 'app/shared/services';
import { ApplicationNameIssuePopupContainerComponent } from 'app/core/components/application-name-issue-popup/application-name-issue-popup-container.component';
import { AgentInfoDataService } from './agent-info-data.service';
import { InspectorPageService, ISourceForAgentInfo } from 'app/routes/inspector-page/inspector-page.service';

@Component({
    selector: 'pp-agent-info-container',
    templateUrl: './agent-info-container.component.html',
    styleUrls: ['./agent-info-container.component.css'],
})
export class AgentInfoContainerComponent implements OnInit, OnDestroy {
    private unsubscribe: Subject<void> = new Subject();
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
        private injector: Injector,
        private inspectorPageService: InspectorPageService,
    ) {}

    ngOnInit() {
        this.urlApplicationName$ = this.newUrlStateNotificationService.onUrlStateChange$.pipe(
            map((urlService: NewUrlStateNotificationService) => {
                return urlService.getPathValue(UrlPathId.APPLICATION).getApplicationName();
            })
        );
        this.connectStore();
        this.inspectorPageService.sourceForAgentInfo$.pipe(
            takeUntil(this.unsubscribe),
            tap(() => this.showLoading = true),
            switchMap(({agentId, selectedTime}: ISourceForAgentInfo) => {
                this.lastRequestParam = [agentId, selectedTime];
                return this.agentInfoDataService.getData(agentId, selectedTime);
            }),
            filter((agentData: IServerAndAgentData) => {
                return !!(agentData && agentData.applicationName);
            })
        ).subscribe((agentData: IServerAndAgentData) => {
            this.agentData = agentData;
            this.dataRequestSuccess = true;
            this.completed();
        }, (_: IServerErrorFormat) => {
            this.dataRequestSuccess = false;
            this.completed();
        });
    }

    ngOnDestroy() {
        this.unsubscribe.next();
        this.unsubscribe.complete();
    }

    private connectStore(): void {
        this.timezone$ = this.storeHelperService.getTimezone(this.unsubscribe);
        this.dateFormat$ = this.storeHelperService.getDateFormat(this.unsubscribe, 1);
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
        }, (_: IServerErrorFormat) => {
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
