import { Component, OnInit, OnDestroy, ViewChild, ViewContainerRef, ComponentFactoryResolver, ComponentRef } from '@angular/core';
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
import { RealTimeWebSocketService, IWebSocketResponse, IWebSocketDataResult, ResponseCode, IActiveThreadCounts } from '../real-time-websocket.service';

import { HELP_VIEWER_LIST, HelpViewerPopupContainerComponent } from 'app/core/components/help-viewer-popup/help-viewer-popup-container.component';
import { NewRealTimeAgentChartComponent } from 'app/core/components/real-time/new/new-real-time-agent-chart.component';
import { NewRealTimeTotalChartComponent } from 'app/core/components/real-time/new/new-real-time-total-chart.component';

// TODO: 나중에 공통으로 추출.
const enum MessageTemplate {
    LOADING = 'LOADING',
    RETRY = 'RETRY',
    NO_DATA = 'NO_DATA',
    NOTHING = 'NOTHING'
}

@Component({
    selector: 'pp-new-real-time-container',
    templateUrl: './new-real-time-container.component.html',
    styleUrls: ['./new-real-time-container.component.css']
})
export class NewRealTimeContainerComponent implements OnInit, OnDestroy {
    @ViewChild('totalChartPlaceHolder', { read: ViewContainerRef} ) totalChartViewContainerRef: ViewContainerRef;
    @ViewChild('agentChartPlaceHolder', { read: ViewContainerRef} ) agentChartViewContainerRef: ViewContainerRef;

    private unsubscribe = new Subject<null>();
    private applicationName = '';
    private serviceType = '';
    private totalComponentRef: ComponentRef<any> = null;
    private agentComponentRef: ComponentRef<any> = null;
    private componentRefMap: any = {};
    totalCount: number;
    firstChartIndex = 0;
    lastChartIndex: number;
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
        this.agentComponentRef = null;
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

        this.publishData(data);
    }
    private setAgentChart(data: IWebSocketDataResult): void {
        if (this.agentComponentRef === null) {
            this.initAgentComponent();
        }

        const { timeStamp, activeThreadCounts } = data;
        const componentInstance = this.agentComponentRef.instance;

        this.totalCount = Object.keys(activeThreadCounts).length;

        componentInstance.activeThreadCounts = this.needPaging() ? this.sliceAgentData(activeThreadCounts) : activeThreadCounts;
        componentInstance.timeStamp = timeStamp;
    }
    private sliceAgentData(data: { [key: string]: IActiveThreadCounts }): { [key: string]: IActiveThreadCounts } {
        this.lastChartIndex = this.realTimeWebSocketService.getPagingSize() - 1;
        const keys = Object.keys(data).slice(this.firstChartIndex, this.lastChartIndex + 1);

        return keys.reduce((acc: { [key: string]: IActiveThreadCounts }, curr: string) => {
            return { ...acc, [curr]: data[curr] };
        }, {});
    }
    private setTotalChart(data: IWebSocketDataResult): void {
        if (this.totalComponentRef === null) {
            this.initTotalComponent();
        }

        const { timeStamp, applicationName, activeThreadCounts } = data;
        const componentInstance = this.totalComponentRef.instance;
        const successData = this.getSuccessData(activeThreadCounts);
        const hasError = successData.length === 0;

        componentInstance.applicationName = applicationName ? applicationName : this.applicationName;
        componentInstance.hasError = hasError;
        componentInstance.errorMessage = hasError ? activeThreadCounts[Object.keys(activeThreadCounts)[0]].message : '';
        componentInstance.timezone = this.timezone;
        componentInstance.dateFormat = this.dateFormat;
        componentInstance.timeStamp = timeStamp;
        componentInstance.data = hasError ? [] : this.getTotalResponseCount(successData);
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
        const componentFactory = this.componentFactoryResolver.resolveComponentFactory(NewRealTimeTotalChartComponent);

        this.totalComponentRef = this.totalChartViewContainerRef.createComponent(componentFactory);
    }
    private initAgentComponent(): void {
        const componentFactory = this.componentFactoryResolver.resolveComponentFactory(NewRealTimeAgentChartComponent);

        this.agentComponentRef = this.agentChartViewContainerRef.createComponent(componentFactory);
        this.agentComponentRef.instance.outOpenThreadDump.subscribe((agentId: string) => {
            this.openThreadDump(agentId);
        });
    }
    needPaging(): boolean {
        return this.totalCount > this.realTimeWebSocketService.getPagingSize();
    }
    getTotalPage(): number[] {
        const totalPage = Math.ceil(this.totalCount / this.realTimeWebSocketService.getPagingSize());

        return Array(totalPage).fill(0).map((v: number, i: number) => i + 1);
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
