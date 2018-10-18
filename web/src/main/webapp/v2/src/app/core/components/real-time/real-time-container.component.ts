import { Component, OnInit, OnDestroy, ViewChild, ViewContainerRef, ComponentFactoryResolver } from '@angular/core';
import { Subject } from 'rxjs';
import { takeUntil, filter } from 'rxjs/operators';

import {
    StoreHelperService,
    WebAppSettingDataService,
    NewUrlStateNotificationService,
    UrlRouteManagerService,
    AnalyticsService,
    TRACKED_EVENT_LIST,
    DynamicPopupService
} from 'app/shared/services';
import { UrlPath, UrlPathId } from 'app/shared/models';
import { RealTimeWebSocketService, IWebSocketResponse, IWebSocketDataResult, ResponseCode, IActiveThreadCounts } from './real-time-websocket.service';
import { RealTimeTotalChartComponent } from './real-time-total-chart.component';
import { RealTimeAgentChartComponent } from './real-time-agent-chart.component';
import { HELP_VIEWER_LIST, HelpViewerPopupContainerComponent } from 'app/core/components/help-viewer-popup/help-viewer-popup-container.component';

// TODO: 나중에 공통으로 추출.
const enum MessageTemplate {
    LOADING = 'LOADING',
    RETRY = 'RETRY',
    NO_DATA = 'NO_DATA',
    NOTHING = 'NOTHING'
}

@Component({
    selector: 'pp-real-time-container',
    templateUrl: './real-time-container.component.html',
    styleUrls: ['./real-time-container.component.css']
})
export class RealTimeContainerComponent implements OnInit, OnDestroy {
    @ViewChild('totalChartPlaceHolder', { read: ViewContainerRef} ) totalChartViewContainerRef: ViewContainerRef;
    @ViewChild('agentChartPlaceHolder', { read: ViewContainerRef} ) agentChartViewContainerRef: ViewContainerRef;

    private unsubscribe = new Subject<null>();
    private applicationName = '';
    private serviceType = '';
    private totalComponentRef: any = null;
    private componentRefMap: any = {};
    totalCount = 0;
    pinUp = true;
    lastHeight: number;
    minHeight = 343;
    maxHeightPadding = 50; // Header Height
    timezone: string;
    dateFormat: string;
    hiddenComponent = true;
    messageTemplate = MessageTemplate.LOADING;
    constructor(
        private componentFactoryResolver: ComponentFactoryResolver,
        private newUrlStateNotificationService: NewUrlStateNotificationService,
        private urlRouteManagerService: UrlRouteManagerService,
        private storeHelperService: StoreHelperService,
        private webAppSettingDataService: WebAppSettingDataService,
        private realTimeWebSocketService: RealTimeWebSocketService,
        private analyticsService: AnalyticsService,
        private dynamicPopupService: DynamicPopupService
    ) {}
    ngOnInit() {
        this.lastHeight = this.webAppSettingDataService.getLayerHeight() || this.minHeight;
        this.newUrlStateNotificationService.onUrlStateChange$.pipe(
            takeUntil(this.unsubscribe),
            filter(() => {
                return this.newUrlStateNotificationService.hasValue(UrlPathId.APPLICATION);
            })
        ).subscribe(() => {
            this.hiddenComponent = true;
            this.resetState();
            this.resetAgentComponentRef();
        });
        this.connectStore();

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
    private connectStore(): void {
        this.storeHelperService.getTimezone(this.unsubscribe).subscribe((timezone: string) => {
            this.timezone = timezone;
        });
        this.storeHelperService.getDateFormat(this.unsubscribe, 0).subscribe((dateFormat: string) => {
            this.dateFormat = dateFormat;
        });
        this.storeHelperService.getServerMapTargetSelected(this.unsubscribe).pipe(
            filter((target: ISelectedTarget) => {
                return target && (target.isMerged === true || target.isMerged === false) ? true : false;
            })
        ).subscribe((target: ISelectedTarget) => {
            this.webAppSettingDataService.useActiveThreadChart().subscribe((use: boolean) => {
                this.hiddenComponent = false;
                const application = target.node[0].split('^');
                if (use === false) {
                    // this.resetState();
                    // this.resetAgentComponentRef();
                    this.hide();
                    return;
                }
                if (this.pinUp === true) {
                    if (this.applicationName !== '') {
                        return;
                    }
                }
                if (target.isWAS) {
                    if (this.isSameWithCurrentTarget(application[0], application[1]) === false) {
                    //     this.resetState();
                    //     this.resetAgentComponentRef();
                    //     this.hide();
                    // } else {
                        this.applicationName = application[0];
                        this.serviceType = application[1];
                        if (this.realTimeWebSocketService.isOpened()) {
                            this.resetAgentComponentRef();
                            this.startDataRequest();
                        } else {
                            this.realTimeWebSocketService.connect();
                        }
                    }
                } else {
                    this.applicationName = application[0];
                    this.serviceType = application[1];
                    this.realTimeWebSocketService.close();
                    this.stopDataRequest();
                    this.resetAgentComponentRef();
                    this.hide();
                }
            });
        });
    }
    private resetAgentComponentRef(): void {
        this.totalChartViewContainerRef.clear();
        this.agentChartViewContainerRef.clear();
        this.totalComponentRef = null;
        this.componentRefMap = {};
    }
    private resetState() {
        this.applicationName = '';
        this.serviceType = '';
        this.pinUp = true;
    }
    private hide() {
        this.messageTemplate = MessageTemplate.NO_DATA;
    }
    private isSameWithCurrentTarget(applicationName: string, serviceType: string): boolean {
        return (this.applicationName === applicationName && this.serviceType === serviceType);
    }
    private startDataRequest(): void {
        this.realTimeWebSocketService.send(this.getRequestDataStr(this.applicationName));
    }
    private stopDataRequest(): void {
        this.realTimeWebSocketService.send(this.getRequestDataStr(''));
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
        /**
         * 0. MERGE (componentRefMap Key, 넘어온 데이터 Key) as Set
         * 1. Set 루핑
         * 2. 넘어온 데이터 object에 대해서 hasOwnProperty(key)
         * 2-1. true && componentRefMap에 없음 => init + update
         * 2-2. true && componentRefMap에 존재 => update
         * 2-3. false => componentRefMap에서 delete, viewContainer에서 지움(.remove(index))
         */
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
                this.componentRefMap[agentName].unsubscription.unsubscribe();
                this.agentChartViewContainerRef.remove(this.componentRefMap[agentName].index);
                delete this.componentRefMap[agentName];
            }
        });
    }
    private setTotalChart({timeStamp, applicationName, activeThreadCounts}: {timeStamp?: number, applicationName?: string, activeThreadCounts: { [key: string]: IActiveThreadCounts }}): void {
        if (this.totalComponentRef === null) {
            this.initTotalComponent();
        }
        const componentInstance = this.totalComponentRef.instance;
        const successData = this.getSuccessData(activeThreadCounts);

        componentInstance.applicationName = applicationName ? applicationName : this.applicationName;
        componentInstance.hasError = successData.length === 0 ? true : false;
        componentInstance.errorMessage = successData.length === 0 ? activeThreadCounts[Object.keys(activeThreadCounts)[0]].message : '';
        componentInstance.timezone = this.timezone;
        componentInstance.dateFormat = this.dateFormat;
        componentInstance.chartData = {
            timeStamp,
            responseCount: successData.length === 0 ? [] : this.getTotalResponseCount(successData)
        };
    }
    private getSuccessData(obj: { [key: string]: IActiveThreadCounts }): IActiveThreadCounts[] {
        return Object.keys(obj)
            .filter((agentName: string) => obj[agentName].code === ResponseCode.SUCCESS)
            .map((agentName: string) => obj[agentName]);
    }
    private getTotalResponseCount(data: IActiveThreadCounts[]): number[] {
        return data.reduce((prev: number[], curr: IActiveThreadCounts) => {
            return prev.map((a: number, i: number) => a + curr.status[i]);
        }, [0, 0, 0, 0]);
    }
    private publishData(data: IWebSocketDataResult): void {
        this.setTotalChart(data);
        this.setAgentChart(data);
    }
    private initTotalComponent(): void {
        const componentFactory = this.componentFactoryResolver.resolveComponentFactory(RealTimeTotalChartComponent);
        this.totalComponentRef = this.totalChartViewContainerRef.createComponent(componentFactory);
    }
    private initAgentComponent(namespace: string) {
        const componentFactory = this.componentFactoryResolver.resolveComponentFactory(RealTimeAgentChartComponent);
        const componentRef = this.agentChartViewContainerRef.createComponent(componentFactory);
        const unsubscription = componentRef.instance.outOpenThreadDump.subscribe((agentId: string) => {
            this.openThreadDump(agentId);
        });

        this.componentRefMap[namespace] = {
            componentRef: componentRef,
            index: this.agentChartViewContainerRef.length - 1,
            unsubscription: unsubscription
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
    onPinUp(): void {
        this.analyticsService.trackEvent(this.pinUp ? TRACKED_EVENT_LIST.PIN_UP_REAL_TIME_CHART : TRACKED_EVENT_LIST.REMOVE_PIN_ON_REAL_TIME_CHART);
        this.pinUp = !this.pinUp;
    }
    openPage(page: number): void {
        this.urlRouteManagerService.openPage([
            UrlPath.REAL_TIME,
            this.newUrlStateNotificationService.getPathValue(UrlPathId.APPLICATION).getUrlStr(),
            '' + page
        ]);
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
    onShowHelp($event: MouseEvent): void {
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.TOGGLE_HELP_VIEWER, HELP_VIEWER_LIST.REAL_TIME);
        const {left, top, width, height} = ($event.target as HTMLElement).getBoundingClientRect();

        this.dynamicPopupService.openPopup({
            data: HELP_VIEWER_LIST.REAL_TIME,
            coord: {
                coordX: left + width / 2,
                coordY: top + height / 2
            },
            component: HelpViewerPopupContainerComponent
        });
    }
}
