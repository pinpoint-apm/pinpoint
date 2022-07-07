import { Component, OnInit, OnDestroy, Inject, ComponentFactoryResolver, Injector, ChangeDetectorRef, ChangeDetectionStrategy } from '@angular/core';
import { Router, NavigationStart, RouterEvent } from '@angular/router';
import { Subject, of, interval, EMPTY, fromEvent } from 'rxjs';
import { takeUntil, filter, switchMap, tap, delayWhen, pluck, startWith, catchError, delay } from 'rxjs/operators';
import { TranslateService } from '@ngx-translate/core';

import {
    NewUrlStateNotificationService,
    UrlRouteManagerService,
    WebAppSettingDataService,
    AnalyticsService,
    TRACKED_EVENT_LIST,
    DynamicPopupService,
    MessageQueueService,
    MESSAGE_TO
} from 'app/shared/services';
import { UrlPathId } from 'app/shared/models';
import { SERVER_MAP_TYPE, ServerMapType, ServerMapData } from 'app/core/components/server-map/class';
import { ServerMapDataService } from './server-map-data.service';
import { LinkContextPopupContainerComponent } from 'app/core/components/link-context-popup/link-context-popup-container.component';
import { NodeContextPopupContainerComponent } from 'app/core/components/node-context-popup/node-context-popup-container.component';
import { ServerMapContextPopupContainerComponent } from 'app/core/components/server-map-context-popup/server-map-context-popup-container.component';
import { ServerErrorPopupContainerComponent } from 'app/core/components/server-error-popup/server-error-popup-container.component';
import { ServerMapRangeHandlerService } from './server-map-range-handler.service';

@Component({
    selector: 'pp-server-map-container',
    templateUrl: './server-map-container.component.html',
    styleUrls: ['./server-map-container.component.css'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class ServerMapContainerComponent implements OnInit, OnDestroy {
    private unsubscribe = new Subject<void>();
    private visibilityHidden = new Subject<void>();

    i18nText: { [key: string]: string } = {
        NO_AGENTS: ''
    };
    mapData: ServerMapData;
    funcServerMapImagePath: Function;
    baseApplicationKey: string;
    showLoading = true;
    useDisable = true;
    isEmpty: boolean;
    interval = 2000;
    shouldRefresh: boolean;
    enableServerMapRealTime: boolean;

    constructor(
        private router: Router,
        private injector: Injector,
        private componentFactoryResolver: ComponentFactoryResolver,
        private translateService: TranslateService,
        private urlRouteManagerService: UrlRouteManagerService,
        private newUrlStateNotificationService: NewUrlStateNotificationService,
        private serverMapDataService: ServerMapDataService,
        private webAppSettingDataService: WebAppSettingDataService,
        private dynamicPopupService: DynamicPopupService,
        private analyticsService: AnalyticsService,
        private cd: ChangeDetectorRef,
        private messageQueueService: MessageQueueService,
        private serverMapRangeHandlerService: ServerMapRangeHandlerService,
        @Inject(SERVER_MAP_TYPE) public type: ServerMapType
    ) {}

    ngOnInit() {
        this.webAppSettingDataService.getExperimentalConfiguration().subscribe(configuration => {
            const enableServerMapRealTime = this.webAppSettingDataService.getExperimentalOption('serverMapRealTime');
            this.enableServerMapRealTime = enableServerMapRealTime === null ? configuration.enableServerMapRealTime.value : enableServerMapRealTime;            
        });
        
        this.funcServerMapImagePath = this.webAppSettingDataService.getServerMapIconPathMakeFunc();
        this.addPageLoadingHandler();
        this.getI18NText();
        this.addEventListener();

        this.messageQueueService.receiveMessage(this.unsubscribe, MESSAGE_TO.SERVER_MAP_DISABLE).subscribe((disable: boolean) => {
            this.useDisable = disable;
            this.cd.detectChanges();
        });

        this.messageQueueService.receiveMessage(this.unsubscribe, MESSAGE_TO.SERVER_MAP_MERGE_STATE_CHANGE).subscribe((mergeState: IServerMapMergeState) => {
            this.mapData = new ServerMapData(this.mapData.getOriginalNodeList(), this.mapData.getOriginalLinkList(), {...this.mapData.getMergeState(), ...mergeState});
            this.shouldRefresh = true;
            this.cd.detectChanges();
        });

        this.newUrlStateNotificationService.onUrlStateChange$.pipe(
            tap((urlService: NewUrlStateNotificationService) => {
                this.showLoading = true;
                this.useDisable = true;
                this.baseApplicationKey = urlService.getPathValue(UrlPathId.APPLICATION).getKeyStr();
                this.shouldRefresh = true;

                if (urlService.isValueChanged(UrlPathId.APPLICATION)) {
                    this.mapData = null;
                }
            }),
            switchMap((urlService: NewUrlStateNotificationService) => {
                if (urlService.isRealTimeMode()) {
                    const endTime = urlService.getUrlServerTimeData();
                    const period = this.webAppSettingDataService.getSystemDefaultPeriod();
                    const range = [endTime - period.getMiliSeconds(), endTime];

                    return this.serverMapRangeHandlerService.onFetchCompleted$.pipe(
                        takeUntil(this.visibilityHidden),
                        delayWhen(({delay: delayTime}: {delay: number}) => interval(delayTime)),
                        pluck('range'),
                        tap(() => this.shouldRefresh = false),
                        startWith(range),
                    );
                } else {
                    const range = [urlService.getStartTimeToNumber(), urlService.getEndTimeToNumber()];
                    return of(range);
                }
            }),
            switchMap((range: number[]) => {
                this.serverMapRangeHandlerService.setReservedNextTo(range[1] + this.interval);
                return this.serverMapDataService.getData(range).pipe(
                    catchError((error: IServerError) => {
                        this.dynamicPopupService.openPopup({
                            data: {
                                title: 'Server Error',
                                contents: error
                            },
                            component: ServerErrorPopupContainerComponent,
                            onCloseCallback: () => {
                                this.urlRouteManagerService.move({
                                    url: [
                                        this.newUrlStateNotificationService.getStartPath()
                                    ],
                                    needServerTimeRequest: false,
                                    queryParams: {
                                        inbound: null,
                                        outbound: null,
                                        bidirectional: null,
                                        wasOnly: null
                                    },
                                });
                            }
                        }, {
                            resolver: this.componentFactoryResolver,
                            injector: this.injector
                        });

                        return EMPTY;
                    }),
                    tap(() => {
                        if (this.newUrlStateNotificationService.isRealTimeMode()) {
                            this.serverMapRangeHandlerService.onFetchCompleted(Date.now());
                        }
                    })
                );
            }),
            takeUntil(this.unsubscribe),
        ).subscribe(({applicationMapData: {nodeDataArray, linkDataArray, range: {from, to}}}: IServerMapInfo) => {
            this.mapData = new ServerMapData(
                nodeDataArray,
                linkDataArray,
                this.mapData ? {...this.mapData.getMergeState()} : {}
            );
            this.isEmpty = this.mapData.getNodeCount() === 0;
            this.messageQueueService.sendMessage({
                to: MESSAGE_TO.SERVER_MAP_DATA_UPDATE,
                param: {serverMapData: this.mapData, range: [from, to]}
            });
            if (this.isEmpty) {
                this.showLoading = false;
            }

            this.cd.detectChanges();
        });
    }

    ngOnDestroy() {
        this.unsubscribe.next();
        this.unsubscribe.complete();
    }

    private addPageLoadingHandler(): void {
        this.router.events.pipe(
            takeUntil(this.unsubscribe),
            filter((e: RouterEvent) => e instanceof NavigationStart)
        ).subscribe(() => {
            this.showLoading = true;
            this.useDisable = true;
            this.cd.detectChanges();
        });
    }

    private addEventListener(): void {
        fromEvent(document, 'visibilitychange').pipe(
            takeUntil(this.unsubscribe),
            filter(() => document.hidden),
            filter(() => this.newUrlStateNotificationService.isRealTimeMode()),
            delay(10000),
            filter(() => document.hidden),
        ).subscribe(() => {
            this.visibilityHidden.next();
        });
    }

    private getI18NText(): void {
        this.translateService.get('COMMON.NO_AGENTS').subscribe((i18n: string) => {
            this.i18nText['NO_AGENTS'] = i18n;
        });
    }

    onMoveNode(): void {
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.MOVE_NODE_IN_SERVER_MAP);
    }

    onRenderCompleted(): void {
        this.showLoading = false;
        this.useDisable = false;
        this.cd.detectChanges();
    }

    onClickNode(nodeData: INodeInfo): void {
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.CLICK_NODE);
        let payload: any;
        if (nodeData.isMerged) {
            this.analyticsService.trackEvent(TRACKED_EVENT_LIST.SHOW_GROUPED_NODE_VIEW);
            payload = {
                isAuthorized: true,
                isNode: true,
                isLink: false,
                isMerged: true,
                isWAS: nodeData.isWas,
                node: nodeData.mergedNodes.map((nodeInfo: any) => {
                    return nodeInfo.key;
                }),
            };
            if (nodeData.mergedSourceNodes) {
                payload.groupedNode = nodeData.mergedSourceNodes.map((nodeInfo: any) => {
                    return nodeInfo.applicationName;
                });
            }
        } else {
            payload = {
                isAuthorized: nodeData.isAuthorized,
                isNode: true,
                isLink: false,
                isMerged: false,
                isWAS: nodeData.isWas,
                node: [nodeData.key],
                hasServerList: nodeData.instanceCount > 0 ? true : false,
                apdexScore: nodeData.apdexScore,
            };
        }
        this.messageQueueService.sendMessage({
            to: MESSAGE_TO.SERVER_MAP_TARGET_SELECT,
            param: payload
        });
    }

    onClickLink(linkData: ILinkInfo): void {
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.CLICK_LINK);
        let payload;
        if (linkData.isMerged) {
            this.analyticsService.trackEvent(TRACKED_EVENT_LIST.SHOW_GROUPED_LINK_VIEW);
            payload = {
                isAuthorized: true,
                isNode: false,
                isLink: true,
                isMerged: true,
                // isSourceMerge: NodeGroup.isGroupKey(linkData.from),
                isSourceMerge: this.mapData.getNodeData(linkData.from).isMerged,
                isWAS: false,
                node: [linkData.from, linkData.to],
                link: (linkData.targetInfo as any).map((linkInfo: any) => {
                    return linkInfo.key;
                }),
                hasServerList: false
            };
        } else {
            payload = {
                isAuthorized: this.mapData.getNodeData(linkData.from).isAuthorized,
                isNode: false,
                isLink: true,
                isMerged: false,
                isWAS: false,
                node: [linkData.from, linkData.to],
                link: [linkData.key],
                hasServerList: false
            };
        }
        this.messageQueueService.sendMessage({
            to: MESSAGE_TO.SERVER_MAP_TARGET_SELECT,
            param: payload
        });
    }

    onContextClickBackground(coord: ICoordinate): void {
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.CONTEXT_CLICK_ON_SERVER_MAP_BACKGROUND);
        this.dynamicPopupService.openPopup({
            data: this.mapData,
            coord,
            component: ServerMapContextPopupContainerComponent
        }, {
            resolver: this.componentFactoryResolver,
            injector: this.injector
        });
    }

    onContextClickNode({key, coord}: {key: string, coord: ICoordinate}): void {
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.CONTEXT_CLICK_ON_SERVER_MAP_NODE);
        const nodeData = this.mapData.getNodeData(key);
        if (nodeData.isWas) {
            this.dynamicPopupService.openPopup({
                data: nodeData,
                coord,
                component: NodeContextPopupContainerComponent
            }, {
                resolver: this.componentFactoryResolver,
                injector: this.injector
            });
        }
    }
    onContextClickLink({key, coord}: {key: string, coord: ICoordinate}): void {
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.CONTEXT_CLICK_ON_SERVER_MAP_LINK);
        this.dynamicPopupService.openPopup({
            data: this.mapData.getLinkData(key),
            coord,
            component: LinkContextPopupContainerComponent
        }, {
            resolver: this.componentFactoryResolver,
            injector: this.injector
        });
    }
}
