import { Component, OnInit, OnDestroy } from '@angular/core';
import { Subject } from 'rxjs';
import { takeUntil, filter, switchMap } from 'rxjs/operators';

import { UrlPathId, UrlPath } from 'app/shared/models';
import { WebAppSettingDataService, NewUrlStateNotificationService, UrlRouteManagerService, AnalyticsService, TRACKED_EVENT_LIST } from 'app/shared/services';
import { NewRealTimeWebSocketService, IWebSocketResponse, IWebSocketDataResult, IActiveThreadCounts } from 'app/core/components/real-time-new/new-real-time-websocket.service';

// TODO: 나중에 공통으로 추출.
const enum MessageTemplate {
    LOADING = 'LOADING',
    RETRY = 'RETRY',
    NO_DATA = 'NO_DATA',
    NOTHING = 'NOTHING'
}

@Component({
    selector: 'pp-new-real-time-paging-container',
    templateUrl: './new-real-time-paging-container.component.html',
    styleUrls: ['./new-real-time-paging-container.component.css']
})
export class NewRealTimePagingContainerComponent implements OnInit, OnDestroy {
    private unsubscribe = new Subject<null>();
    private applicationName = '';
    private serviceType = '';
    totalCount: number;
    firstChartIndex: number;
    lastChartIndex: number;
    indexLimit: number;
    currentPage: number;
    timeStamp: number;
    activeThreadCounts: { [key: string]: IActiveThreadCounts };
    messageTemplate = MessageTemplate.LOADING;
    constructor(
        private newUrlStateNotificationService: NewUrlStateNotificationService,
        private webAppSettingDataService: WebAppSettingDataService,
        private realTimeWebSocketService: NewRealTimeWebSocketService,
        private urlRouteManagerService: UrlRouteManagerService,
        private analyticsService: AnalyticsService,
    ) {}
    ngOnInit() {
        this.newUrlStateNotificationService.onUrlStateChange$.pipe(
            takeUntil(this.unsubscribe),
            filter(() => {
                return this.newUrlStateNotificationService.hasValue(UrlPathId.APPLICATION);
            }),
            switchMap(() => {
                return this.webAppSettingDataService.useActiveThreadChart().pipe(
                    filter((useActiveThreadChart: boolean) => {
                        return useActiveThreadChart ? true : (this.hide(), false);
                    })
                );
            })
        ).subscribe(() => {
            this.resetState();
            this.messageTemplate = MessageTemplate.LOADING;
            this.applicationName = this.newUrlStateNotificationService.getPathValue(UrlPathId.APPLICATION).getApplicationName();
            this.serviceType = this.newUrlStateNotificationService.getPathValue(UrlPathId.APPLICATION).getServiceType();
            this.currentPage = Number(this.newUrlStateNotificationService.getPathValue(UrlPathId.PAGE));
            this.firstChartIndex = (this.currentPage - 1) * this.realTimeWebSocketService.getPagingSize();
            this.indexLimit = this.currentPage * this.realTimeWebSocketService.getPagingSize() - 1;
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
    ngOnDestroy() {
        this.realTimeWebSocketService.close();
        this.unsubscribe.next();
        this.unsubscribe.complete();
    }
    private resetState() {
        this.applicationName = '';
        this.serviceType = '';
        this.activeThreadCounts = null;
    }
    private hide() {
        this.messageTemplate = MessageTemplate.NO_DATA;
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

        this.totalCount = Object.keys(activeThreadCounts).length;
        this.timeStamp = timeStamp;
        this.activeThreadCounts = this.sliceAgentData(activeThreadCounts);
    }
    private sliceAgentData(data: { [key: string]: IActiveThreadCounts }): { [key: string]: IActiveThreadCounts } {
        this.lastChartIndex = this.totalCount - 1 <= this.indexLimit ? this.totalCount - 1 : this.indexLimit;
        const keys = Object.keys(data).slice(this.firstChartIndex, this.lastChartIndex + 1);

        return keys.reduce((acc: { [key: string]: IActiveThreadCounts }, curr: string) => {
            return { ...acc, [curr]: data[curr] };
        }, {});
    }
    retryConnection(): void {
        this.messageTemplate = MessageTemplate.LOADING;
        this.realTimeWebSocketService.connect();
    }
    onOpenThreadDump(agentId: string): void {
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.OPEN_THREAD_DUMP);
        this.urlRouteManagerService.openPage([
            UrlPath.THREAD_DUMP,
            this.newUrlStateNotificationService.getPathValue(UrlPathId.APPLICATION).getUrlStr(),
            agentId,
            '' + Date.now()
        ]);
    }
    onRenderCompleted(): void {
        this.messageTemplate = MessageTemplate.NOTHING;
    }
}
