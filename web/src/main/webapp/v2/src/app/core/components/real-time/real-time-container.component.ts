import { Component, OnInit, OnDestroy, AfterViewInit, ComponentFactoryResolver, Injector } from '@angular/core';
import { Subject, Observable, fromEvent } from 'rxjs';
import { takeUntil, filter, delay } from 'rxjs/operators';

import {
    StoreHelperService,
    WebAppSettingDataService,
    NewUrlStateNotificationService,
    AnalyticsService,
    TRACKED_EVENT_LIST,
    DynamicPopupService,
    UrlRouteManagerService
} from 'app/shared/services';
import { RealTimeWebSocketService, IWebSocketResponse, IWebSocketDataResult, IActiveThreadCounts } from 'app/core/components/real-time/real-time-websocket.service';
import { HELP_VIEWER_LIST, HelpViewerPopupContainerComponent } from 'app/core/components/help-viewer-popup/help-viewer-popup-container.component';
import { UrlPathId, UrlPath } from 'app/shared/models';
import { IParsedATC } from './real-time-chart.component';
import { getATCforAgent, getATCforTotal, getFilteredATC } from './real-time-util';

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
export class RealTimeContainerComponent implements OnInit, AfterViewInit, OnDestroy {
    private unsubscribe = new Subject<null>();
    private serviceType = '';
    activeOnly = false;
    isPinUp = true;
    lastHeight: number;
    minHeight = 343;
    maxHeightPadding = 50; // Header Height
    timezone$: Observable<string>;
    dateFormat$: Observable<string>;
    applicationName = '';
    timeStamp: number;
    activeThreadCounts: { [key: string]: IActiveThreadCounts };
    atcForAgent: { [key: string]: IParsedATC };
    atcForTotal: { [key: string]: IParsedATC };
    totalCount: number;
    sum: number[];
    hiddenComponent = true;
    messageTemplate = MessageTemplate.LOADING;
    // messageTemplate: MessageTemplate;
    constructor(
        private newUrlStateNotificationService: NewUrlStateNotificationService,
        private urlRouteManagerService: UrlRouteManagerService,
        private storeHelperService: StoreHelperService,
        private webAppSettingDataService: WebAppSettingDataService,
        private realTimeWebSocketService: RealTimeWebSocketService,
        private analyticsService: AnalyticsService,
        private dynamicPopupService: DynamicPopupService,
        private componentFactoryResolver: ComponentFactoryResolver,
        private injector: Injector
    ) {}
    ngOnInit() {
        this.lastHeight = this.webAppSettingDataService.getLayerHeight() || this.minHeight;
        this.newUrlStateNotificationService.onUrlStateChange$.pipe(
            takeUntil(this.unsubscribe),
        ).subscribe(() => {
            // this.hiddenComponent = true;
            this.resetState();
            this.messageTemplate = MessageTemplate.LOADING;
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
    ngAfterViewInit() {
        this.addEventListener();
    }
    ngOnDestroy() {
        this.realTimeWebSocketService.close();
        this.unsubscribe.next();
        this.unsubscribe.complete();
    }
    private connectStore(): void {
        this.timezone$ = this.storeHelperService.getTimezone(this.unsubscribe);
        this.dateFormat$ = this.storeHelperService.getDateFormat(this.unsubscribe, 0);
        this.storeHelperService.getServerMapTargetSelected(this.unsubscribe).pipe(
            filter((target: ISelectedTarget) => {
                return target && (target.isMerged === true || target.isMerged === false) ? (this.hiddenComponent = false, true) : (this.hiddenComponent = true, false);
            }),
            filter(() => {
                return !(this.isPinUp && this.applicationName !== '');
            })
        ).subscribe((target: ISelectedTarget) => {
            const [applicationName, serviceType] = target.node[0].split('^');

            if (target.isWAS) {
                if (!this.isSameWithCurrentTarget(applicationName, serviceType)) {
                    this.applicationName = applicationName;
                    this.serviceType = serviceType;
                    this.realTimeWebSocketService.isOpened() ? this.startDataRequest() : this.realTimeWebSocketService.connect();
                }
            } else {
                this.applicationName = applicationName;
                this.serviceType = serviceType;
                this.realTimeWebSocketService.close();
                this.stopDataRequest();
                this.hide();
            }
        });
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
        this.isPinUp = true;
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
        this.atcForTotal = getATCforTotal(applicationName, activeThreadCounts);
        this.sum = this.atcForTotal[Object.keys(this.atcForTotal)[0]].status;
        this.totalCount = Object.keys(this.atcForAgent).length;
        this.activeThreadCounts = activeThreadCounts;
    }

    retryConnection(): void {
        this.messageTemplate = MessageTemplate.LOADING;
        this.realTimeWebSocketService.connect();
    }
    onPinUp(): void {
        this.analyticsService.trackEvent(this.isPinUp ? TRACKED_EVENT_LIST.PIN_UP_REAL_TIME_CHART : TRACKED_EVENT_LIST.REMOVE_PIN_ON_REAL_TIME_CHART);
        this.isPinUp = !this.isPinUp;
    }
    onOpenPage(page: number): void {
        this.urlRouteManagerService.openPage([
            UrlPath.REAL_TIME,
            this.newUrlStateNotificationService.getPathValue(UrlPathId.APPLICATION).getUrlStr(),
            '' + page,
            `?activeOnly=${this.activeOnly}`
        ]);
    }
    onChangeActiveOnlyToggle(activeOnly: boolean): void {
        this.activeOnly = activeOnly;
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
        }, {
            resolver: this.componentFactoryResolver,
            injector: this.injector
        });
    }
    onRenderCompleted(): void {
        this.messageTemplate = MessageTemplate.NOTHING;
    }
}
