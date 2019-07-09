import { Component, OnInit, OnDestroy, Inject, ComponentFactoryResolver, Injector } from '@angular/core';
import { Router, NavigationStart, RouterEvent } from '@angular/router';
import { Subject } from 'rxjs';
import { takeUntil, filter, map, switchMap } from 'rxjs/operators';
import { TranslateService } from '@ngx-translate/core';

import {
    StoreHelperService,
    NewUrlStateNotificationService,
    UrlRouteManagerService,
    WebAppSettingDataService,
    AnalyticsService,
    TRACKED_EVENT_LIST,
    DynamicPopupService
} from 'app/shared/services';
import { Actions } from 'app/shared/store';
import { UrlPathId } from 'app/shared/models';
import { EndTime } from 'app/core/models';
import { SERVER_MAP_TYPE, ServerMapType, NodeGroup, ServerMapData } from 'app/core/components/server-map/class';
import { ServerMapDataService } from './server-map-data.service';
import { LinkContextPopupContainerComponent } from 'app/core/components/link-context-popup/link-context-popup-container.component';
import { ServerMapContextPopupContainerComponent } from 'app/core/components/server-map-context-popup/server-map-context-popup-container.component';
import { ServerErrorPopupContainerComponent } from 'app/core/components/server-error-popup';

@Component({
    selector: 'pp-server-map-container',
    templateUrl: './server-map-container.component.html',
    styleUrls: ['./server-map-container.component.css']
})
export class ServerMapContainerComponent implements OnInit, OnDestroy {
    private unsubscribe: Subject<null> = new Subject();
    i18nText: { [key: string]: string } = {
        NO_AGENTS: ''
    };
    mapData: ServerMapData;
    funcServerMapImagePath: Function;
    baseApplicationKey: string;
    showOverview = false;
    showLoading = true;
    useDisable = true;
    endTime: string;
    period: string;
    constructor(
        private router: Router,
        private injector: Injector,
        private componentFactoryResolver: ComponentFactoryResolver,
        private storeHelperService: StoreHelperService,
        private translateService: TranslateService,
        private urlRouteManagerService: UrlRouteManagerService,
        private newUrlStateNotificationService: NewUrlStateNotificationService,
        private serverMapDataService: ServerMapDataService,
        private webAppSettingDataService: WebAppSettingDataService,
        private dynamicPopupService: DynamicPopupService,
        private analyticsService: AnalyticsService,
        @Inject(SERVER_MAP_TYPE) public type: ServerMapType
    ) {}
    ngOnInit() {
        this.funcServerMapImagePath = this.webAppSettingDataService.getServerMapIconPathMakeFunc();
        this.addPageLoadingHandler();
        this.getI18NText();

        this.newUrlStateNotificationService.onUrlStateChange$.pipe(
            takeUntil(this.unsubscribe),
            map((urlService: NewUrlStateNotificationService) => {
                if (urlService.isRealTimeMode()) {
                    const endTime = urlService.getUrlServerTimeData();
                    const period = this.webAppSettingDataService.getSystemDefaultPeriod();

                    this.initVarBeforeDataLoad(
                        EndTime.formatDate(endTime),
                        period.getValueWithTime(),
                        urlService.getPathValue(UrlPathId.APPLICATION)
                    );

                    return [endTime - period.getMiliSeconds(), endTime];
                } else {
                    this.initVarBeforeDataLoad(
                        urlService.getPathValue(UrlPathId.END_TIME).getEndTime(),
                        urlService.getPathValue(UrlPathId.PERIOD).getValueWithTime(),
                        urlService.getPathValue(UrlPathId.APPLICATION)
                    );

                    return [urlService.getStartTimeToNumber(), urlService.getEndTimeToNumber()];
                }
            }),
            switchMap((range: number[]) => this.serverMapDataService.getData(range))
        ).subscribe((res: IServerMapInfo) => {
            this.mapData = new ServerMapData(res.applicationMapData.nodeDataArray, res.applicationMapData.linkDataArray);
            this.storeHelperService.dispatch(new Actions.UpdateServerMapData(this.mapData));
            if (!this.hasNodeData()) {
                this.showLoading = false;
            }
        }, (error: IServerErrorFormat) => {
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
                        needServerTimeRequest: false
                    });
                }
            }, {
                resolver: this.componentFactoryResolver,
                injector: this.injector
            });
        });
        this.connectStore();
    }
    ngOnDestroy() {
        this.unsubscribe.next();
        this.unsubscribe.complete();
    }
    private connectStore(): void {
        this.storeHelperService.getServerMapDisableState(this.unsubscribe).subscribe((disabled: boolean) => {
            this.useDisable = disabled;
        });
    }
    private addPageLoadingHandler(): void {
        this.router.events.pipe(
            filter((e: RouterEvent) => {
                return e instanceof NavigationStart;
            })
        ).subscribe(() => {
            this.showLoading = true;
            this.useDisable = true;
        });
    }
    private getI18NText(): void {
        this.translateService.get('COMMON.NO_AGENTS').subscribe((i18n: string) => {
            this.i18nText['NO_AGENTS'] = i18n;
        });
    }
    private initVarBeforeDataLoad(endTime: string, period: string, application: IApplication): void {
        this.endTime = endTime;
        this.period = period;
        this.showLoading = true;
        this.useDisable = true;
        this.baseApplicationKey = application.getKeyStr();
    }
    hasNodeData(): boolean {
        return this.mapData.getNodeCount() !== 0;
    }
    onRenderCompleted({showOverView}: {showOverView: boolean}): void {
        this.showLoading = false;
        this.useDisable = false;
        this.showOverview = this.hasNodeData() && showOverView;
    }
    onClickBackground($event: any): void {}
    onClickNode(nodeData: any): void {
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.CLICK_NODE);
        let payload: any;
        if (NodeGroup.isGroupKey(nodeData.key)) {
            this.analyticsService.trackEvent(TRACKED_EVENT_LIST.SHOW_GROUPED_NODE_VIEW);
            payload = {
                period: this.period,
                endTime: this.endTime,
                isAuthorized: true,
                isNode: true,
                isLink: false,
                isMerged: true,
                isWAS: nodeData.isWas,
                node: nodeData.mergedNodes.map((nodeInfo: any) => {
                    return nodeInfo.key;
                })
            };
            if (nodeData.mergedSourceNodes) {
                payload.groupedNode = nodeData.mergedSourceNodes.map((nodeInfo: any) => {
                    return nodeInfo.applicationName;
                });
            }
        } else {
            payload = {
                clickParam: nodeData.clickParam,
                period: this.period,
                endTime: this.endTime,
                isAuthorized: nodeData.isAuthorized,
                isNode: true,
                isLink: false,
                isMerged: false,
                isWAS: nodeData.isWas,
                node: [nodeData.key],
                hasServerList: nodeData.instanceCount > 0 ? true : false
            };
        }
        this.storeHelperService.dispatch(new Actions.UpdateServerMapTargetSelected(payload));
    }
    onClickLink(linkData: any): void {
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.CLICK_LINK);
        let payload;
        if (NodeGroup.isGroupKey(linkData.key)) {
            this.analyticsService.trackEvent(TRACKED_EVENT_LIST.SHOW_GROUPED_LINK_VIEW);
            payload = {
                period: this.period,
                endTime: this.endTime,
                isAuthorized: true,
                isNode: false,
                isLink: true,
                isMerged: true,
                isSourceMerge: NodeGroup.isGroupKey(linkData.from),
                isWAS: false,
                node: [linkData.from],
                link: linkData.targetInfo.map((linkInfo: any) => {
                    return linkInfo.key;
                }),
                hasServerList: false
            };
        } else {
            payload = {
                period: this.period,
                endTime: this.endTime,
                isAuthorized: this.mapData.getNodeData(linkData.from).isAuthorized,
                isNode: false,
                isLink: true,
                isMerged: false,
                isWAS: false,
                node: [linkData.from],
                link: [linkData.key],
                hasServerList: false
            };
        }
        this.storeHelperService.dispatch(new Actions.UpdateServerMapTargetSelected(payload));
    }
    onDoubleClickBackground($event: any): void {}
    onContextClickBackground(coord: ICoordinate): void {
        this.dynamicPopupService.openPopup({
            data: this.mapData,
            coord,
            component: ServerMapContextPopupContainerComponent
        }, {
            resolver: this.componentFactoryResolver,
            injector: this.injector
        });
    }
    onContextClickNode($event: any): void {}
    onContextClickLink({key, coord}: {key: string, coord: ICoordinate}): void {
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
