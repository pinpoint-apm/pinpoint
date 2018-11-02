import { Component, OnInit, OnDestroy, ViewChild, ViewContainerRef, ComponentFactoryResolver } from '@angular/core';
import { Subject } from 'rxjs';
import { takeUntil, filter } from 'rxjs/operators';

import { UrlPathId } from 'app/shared/models';
import { WebAppSettingDataService, NewUrlStateNotificationService } from 'app/shared/services';
import { RealTimeWebSocketService, IWebSocketResponse, IWebSocketDataResult, ResponseCode, IActiveThreadCounts } from './real-time-websocket.service';
import { RealTimeAgentChartComponent } from './real-time-agent-chart.component';

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
export class RealTimePagingContainerComponent implements OnInit, OnDestroy {
    @ViewChild('agentChartPlaceHolder', { read: ViewContainerRef} ) agentChartViewContainerRef: ViewContainerRef;

    private unsubscribe = new Subject<null>();
    private applicationName = '';
    private serviceType = '';
    private componentRefMap: any = {};
    totalCount = 0;
    startCount: number;
    endCount: number;
    currentPage: number;
    messageTemplate = MessageTemplate.LOADING;
    constructor(
        private componentFactoryResolver: ComponentFactoryResolver,
        private newUrlStateNotificationService: NewUrlStateNotificationService,
        private webAppSettingDataService: WebAppSettingDataService,
        private realTimeWebSocketService: RealTimeWebSocketService
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
            this.currentPage = Number(this.newUrlStateNotificationService.getPathValue(UrlPathId.PAGE));
            this.startCount = this.currentPage * (this.realTimeWebSocketService.getPagingSize() - 1);
            this.endCount = this.startCount + this.realTimeWebSocketService.getPagingSize();

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
        this.totalCount = Object.keys(data.activeThreadCounts).length;
        this.publishData(data);
    }
    private setAgentChart({timeStamp, activeThreadCounts}: {timeStamp?: number, activeThreadCounts: { [key: string]: IActiveThreadCounts }}): void {
        const mergedKeySet = new Set([...Object.keys(this.componentRefMap), ...Object.keys(activeThreadCounts)]);

        mergedKeySet.forEach((agentName: string) => {
            if (activeThreadCounts.hasOwnProperty(agentName)) {
                if (typeof this.componentRefMap[agentName] === 'undefined') {
                    this.initAgentComponent(agentName);
                }
                const componentInstance = this.componentRefMap[agentName].componentRef.instance;
                const isResponseSuccess = activeThreadCounts[agentName].code === ResponseCode.SUCCESS;

                componentInstance.agentName = agentName;
                componentInstance.hasError = isResponseSuccess ? false : true;
                componentInstance.errorMessage = isResponseSuccess ? '' : activeThreadCounts[agentName].message;
                componentInstance.chartData = {
                    timeStamp,
                    responseCount: isResponseSuccess ? activeThreadCounts[agentName].status : []
                };
            } else {
                this.agentChartViewContainerRef.remove(this.componentRefMap[agentName].index);
                delete this.componentRefMap[agentName];
            }
        });
    }
    private publishData(data: IWebSocketDataResult): void {
        this.setAgentChart(data);
    }
    private initAgentComponent(namespace: string) {
        const componentFactory = this.componentFactoryResolver.resolveComponentFactory(RealTimeAgentChartComponent);
        const componentRef = this.agentChartViewContainerRef.createComponent(componentFactory);
        this.componentRefMap[namespace] = {
            componentRef: componentRef,
            index: this.agentChartViewContainerRef.length - 1
        };
    }
    needPaging(): boolean {
        return this.totalCount > this.realTimeWebSocketService.getPagingSize();
    }
    getTotalPage(): number[] {
        const totalPage = Math.ceil(this.totalCount / this.realTimeWebSocketService.getPagingSize());
        const pages = [];
        for (let i = totalPage ; i > 1 ; i-- ) {
            pages.push(i);
        }
        return pages;
    }
    retryConnection(): void {
        this.messageTemplate = MessageTemplate.LOADING;
        this.realTimeWebSocketService.connect();
    }
}
