import { Component, OnInit, OnDestroy, AfterViewInit } from '@angular/core';
import { Subject, fromEvent } from 'rxjs';
import { takeUntil, filter, delay } from 'rxjs/operators';

import { UrlPathId, UrlPath, UrlQuery } from 'app/shared/models';
import { NewUrlStateNotificationService, UrlRouteManagerService, AnalyticsService, TRACKED_EVENT_LIST } from 'app/shared/services';
import { RealTimeWebSocketService, IWebSocketResponse, IWebSocketDataResult, IActiveThreadCounts } from 'app/core/components/real-time/real-time-websocket.service';
import { IParsedATC } from './real-time-chart.component';
import { getATCforAgent, getTotalResponseCount, getSuccessDataList, getFilteredATC } from './real-time-util';

// TODO: 나중에 공통으로 추출.
const enum MessageTemplate {
    LOADING = 'LOADING',
    RETRY = 'RETRY',
    NO_DATA = 'NO_DATA',
    NOTHING = 'NOTHING'
}

@Component({
    selector: 'pp-real-time-paging-container',
    templateUrl: './real-time-paging-container.component.html',
    styleUrls: ['./real-time-paging-container.component.css']
})
export class RealTimePagingContainerComponent implements OnInit, AfterViewInit, OnDestroy {
    private unsubscribe = new Subject<null>();
    private applicationName = '';
    private serviceType = '';
    activeOnly: boolean;
    timeStamp: number;
    activeThreadCounts: { [key: string]: IActiveThreadCounts };
    atcForAgent: { [key: string]: IParsedATC };
    messageTemplate = MessageTemplate.LOADING;
    totalCount: number;
    sum: number[];
    currentPage: number;
    constructor(
        private newUrlStateNotificationService: NewUrlStateNotificationService,
        private realTimeWebSocketService: RealTimeWebSocketService,
        private urlRouteManagerService: UrlRouteManagerService,
        private analyticsService: AnalyticsService,
    ) {}
    ngOnInit() {
        this.newUrlStateNotificationService.onUrlStateChange$.pipe(
            takeUntil(this.unsubscribe),
        ).subscribe((urlService: NewUrlStateNotificationService) => {
            this.resetState();
            this.activeOnly = !!urlService.getQueryValue(UrlQuery.ACTIVE_ONLY);
            this.currentPage = urlService.getPathValue(UrlPathId.PAGE);
            this.messageTemplate = MessageTemplate.LOADING;
            this.applicationName = urlService.getPathValue(UrlPathId.APPLICATION).getApplicationName();
            this.serviceType = urlService.getPathValue(UrlPathId.APPLICATION).getServiceType();
            this.realTimeWebSocketService.isOpened() ? this.startDataRequest() : this.realTimeWebSocketService.connect();
        });

        this.realTimeWebSocketService.onMessage$.pipe(
            takeUntil(this.unsubscribe)
        ).subscribe((response: IWebSocketResponse) => {
            switch (response.type) {
                case 'open':
                    this.onOpen();
                    break;
                case 'close':
                    this.onClose();
                    break;
                case 'retry':
                    this.onRetry();
                    break;
                case 'message':
                    this.onMessage(response.message as IWebSocketDataResult);
                    break;
            }
        });
    }
    ngAfterViewInit() {
        this.addEventListener();
    }
    ngOnDestroy() {
        this.realTimeWebSocketService.close();
        this.unsubscribe.next();
        this.unsubscribe.complete();
    }
    private addEventListener(): void {
        const visibility$ = fromEvent(document, 'visibilitychange').pipe(takeUntil(this.unsubscribe));

        // visible
        visibility$.pipe(
            filter(() => {
                return !document.hidden;
            }),
            filter(() => {
                return !this.realTimeWebSocketService.isOpened();
            })
        ).subscribe(() => {
            this.onRetry();
        });

        // hidden
        visibility$.pipe(
            filter(() => {
                return document.hidden;
            }),
            delay(60000),
            filter(() => {
                return document.hidden;
            }),
        ).subscribe(() => {
            this.realTimeWebSocketService.close();
        });
    }
    private resetState() {
        this.applicationName = '';
        this.serviceType = '';
        this.activeThreadCounts = null;
    }
    private startDataRequest(): void {
        this.realTimeWebSocketService.send(this.getRequestDataStr(this.applicationName));
    }
    private getRequestDataStr(name: string): object {
        return {
            type: 'REQUEST',
            command: 'activeThreadCount',
            parameters: {
                applicationName: name
            }
        };
    }
    private onOpen(): void {
        this.startDataRequest();
    }
    private onClose(): void {
        this.messageTemplate = MessageTemplate.RETRY;
        this.activeThreadCounts = null;
    }
    private onRetry(): void {
        this.retryConnection();
    }
    private onMessage(data: IWebSocketDataResult): void {
        // this.messageTemplate = MessageTemplate.NOTHING;
        const { timeStamp, applicationName, activeThreadCounts } = data;

        if (applicationName && applicationName !== this.applicationName) {
            return;
        }

        this.timeStamp = timeStamp;
        this.atcForAgent = this.activeOnly ? getFilteredATC(getATCforAgent(this.activeThreadCounts, activeThreadCounts)) : getATCforAgent(this.activeThreadCounts, activeThreadCounts);
        this.sum = getTotalResponseCount(getSuccessDataList(this.atcForAgent));
        this.totalCount = Object.keys(this.atcForAgent).length;
        this.activeThreadCounts = activeThreadCounts;
    }
    retryConnection(): void {
        this.messageTemplate = MessageTemplate.LOADING;
        this.realTimeWebSocketService.connect();
    }
    onRenderCompleted(): void {
        this.messageTemplate = MessageTemplate.NOTHING;
    }
    onOpenPage(page: number): void {
        this.urlRouteManagerService.openPage({
            path: [
                UrlPath.REAL_TIME,
                this.newUrlStateNotificationService.getPathValue(UrlPathId.APPLICATION).getUrlStr(),
                '' + page
            ],
            queryParam: {
                activeOnly: this.activeOnly
            }
        });
    }
    onChangeActiveOnlyToggle(activeOnly: boolean): void {
        this.activeOnly = activeOnly;
    }
    onOpenThreadDump(agentId: string): void {
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.OPEN_THREAD_DUMP);
        this.urlRouteManagerService.openPage({
            path: [
                UrlPath.THREAD_DUMP,
                this.newUrlStateNotificationService.getPathValue(UrlPathId.APPLICATION).getUrlStr(),
                agentId,
                '' + Date.now()
            ]
        });
    }
}
