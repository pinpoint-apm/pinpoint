import { Component, OnInit, OnDestroy, ViewChild, ViewContainerRef, ComponentFactoryResolver, ComponentRef } from '@angular/core';
import { Subject } from 'rxjs';
import { takeUntil, filter } from 'rxjs/operators';

import { UrlPathId, UrlPath } from 'app/shared/models';
import { WebAppSettingDataService, NewUrlStateNotificationService, UrlRouteManagerService, AnalyticsService, TRACKED_EVENT_LIST } from 'app/shared/services';
import { RealTimeWebSocketService, IWebSocketResponse, IWebSocketDataResult, ResponseCode, IActiveThreadCounts } from '../real-time-websocket.service';
import { NewRealTimeAgentChartComponent } from 'app/core/components/real-time/new/new-real-time-agent-chart.component';

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
    @ViewChild('agentChartPlaceHolder', { read: ViewContainerRef} ) agentChartViewContainerRef: ViewContainerRef;

    private unsubscribe = new Subject<null>();
    private applicationName = '';
    private serviceType = '';
    private componentRefMap: any = {};
    private agentComponentRef: ComponentRef<any> = null;
    totalCount: number;
    firstChartIndex: number;
    lastChartIndex: number;
    indexLimit: number;
    currentPage: number;
    messageTemplate = MessageTemplate.LOADING;
    constructor(
        private componentFactoryResolver: ComponentFactoryResolver,
        private newUrlStateNotificationService: NewUrlStateNotificationService,
        private webAppSettingDataService: WebAppSettingDataService,
        private realTimeWebSocketService: RealTimeWebSocketService,
        private urlRouteManagerService: UrlRouteManagerService,
        private analyticsService: AnalyticsService,
    ) {}
    ngOnInit() {
        this.newUrlStateNotificationService.onUrlStateChange$.pipe(
            takeUntil(this.unsubscribe),
            filter(() => {
                return this.newUrlStateNotificationService.hasValue(UrlPathId.APPLICATION);
            })
        ).subscribe(() => {
            this.applicationName = this.newUrlStateNotificationService.getPathValue(UrlPathId.APPLICATION).getApplicationName();
            this.serviceType = this.newUrlStateNotificationService.getPathValue(UrlPathId.APPLICATION).getServiceType();
            this.currentPage = Number.parseInt(this.newUrlStateNotificationService.getPathValue(UrlPathId.PAGE));
            // this.startCount = this.currentPage * (this.realTimeWebSocketService.getPagingSize() - 1);
            // this.endCount = this.startCount + this.realTimeWebSocketService.getPagingSize();
            this.firstChartIndex = (this.currentPage - 1) * this.realTimeWebSocketService.getPagingSize();
            // this.endCount = this.currentPage * this.realTimeWebSocketService.getPagingSize();
            this.indexLimit = this.currentPage * this.realTimeWebSocketService.getPagingSize() - 1;

            this.webAppSettingDataService.useActiveThreadChart().subscribe((use: boolean) => {
                if (use === false) {
                    this.resetState();
                    this.resetAgentComponentRef();
                    this.hide();
                    return;
                }
                if (this.realTimeWebSocketService.isOpened()) {
                    this.resetAgentComponentRef();
                    this.startDataRequest();
                } else {
                    this.realTimeWebSocketService.connect();
                }
            });
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
        this.unsubscribe.next();
        this.unsubscribe.complete();
    }
    private resetAgentComponentRef(): void {
        this.agentChartViewContainerRef.clear();
        this.componentRefMap = {};
    }
    private resetState() {
        this.applicationName = '';
        this.serviceType = '';
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
        this.messageTemplate = MessageTemplate.NOTHING;
        if (data.applicationName && data.applicationName !== this.applicationName) {
            return;
        }

        this.publishData(data);
    }
    private setAgentChart(data: IWebSocketDataResult): void {
        if (this.agentComponentRef === null) {
            this.initAgentComponent();
        }

        const { timeStamp, activeThreadCounts } = data;
        const componentInstance = this.agentComponentRef.instance;

        this.totalCount = Object.keys(activeThreadCounts).length;

        componentInstance.activeThreadCounts = this.sliceAgentData(activeThreadCounts);
        componentInstance.timeStamp = timeStamp;
    }
    private sliceAgentData(data: { [key: string]: IActiveThreadCounts }): { [key: string]: IActiveThreadCounts } {
        this.lastChartIndex = this.totalCount - 1 <= this.indexLimit ? this.totalCount - 1 : this.indexLimit;
        const keys = Object.keys(data).slice(this.firstChartIndex, this.lastChartIndex + 1);

        return keys.reduce((acc: { [key: string]: IActiveThreadCounts }, curr: string) => {
            return { ...acc, [curr]: data[curr] };
        }, {});
    }
    private publishData(data: IWebSocketDataResult): void {
        this.setAgentChart(data);
    }
    private initAgentComponent() {
        const componentFactory = this.componentFactoryResolver.resolveComponentFactory(NewRealTimeAgentChartComponent);

        this.agentComponentRef = this.agentChartViewContainerRef.createComponent(componentFactory);
        this.agentComponentRef.instance.outOpenThreadDump.subscribe((agentId: string) => {
            this.openThreadDump(agentId);
        });
    }
    retryConnection(): void {
        this.messageTemplate = MessageTemplate.LOADING;
        this.realTimeWebSocketService.connect();
    }
    openThreadDump(agentId: string): void {
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.OPEN_THREAD_DUMP);
        this.urlRouteManagerService.openPage([
            UrlPath.THREAD_DUMP,
            this.newUrlStateNotificationService.getPathValue(UrlPathId.APPLICATION).getUrlStr(),
            agentId,
            '' + Date.now()
        ]);
    }
}
