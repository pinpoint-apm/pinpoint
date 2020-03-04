import { Component, OnInit, OnDestroy, Inject, ComponentFactoryResolver, Injector, ChangeDetectorRef, ChangeDetectionStrategy } from '@angular/core';
import { Router, NavigationStart, RouterEvent } from '@angular/router';
import { Subject } from 'rxjs';
import { takeUntil, filter } from 'rxjs/operators';
import { TranslateService } from '@ngx-translate/core';

import {
    StoreHelperService,
    NewUrlStateNotificationService,
    WebAppSettingDataService,
    AnalyticsService,
    TRACKED_EVENT_LIST,
    DynamicPopupService,
    MessageQueueService,
    MESSAGE_TO
} from 'app/shared/services';
import { Actions } from 'app/shared/store';
import { UrlPathId, UrlQuery } from 'app/shared/models';
import { Filter } from 'app/core/models';
import { SERVER_MAP_TYPE, ServerMapType, NodeGroup, ServerMapData, MergeServerMapData } from 'app/core/components/server-map/class';
import { ServerMapForFilteredMapDataService } from './server-map-for-filtered-map-data.service';
import { LinkContextPopupContainerComponent } from 'app/core/components/link-context-popup/link-context-popup-container.component';
import { ServerMapContextPopupContainerComponent } from 'app/core/components/server-map-context-popup/server-map-context-popup-container.component';
import { isEmpty } from 'app/core/utils/util';

@Component({
    selector: 'pp-server-map-for-filtered-map-container',
    templateUrl: './server-map-for-filtered-map-container.component.html',
    styleUrls: ['./server-map-for-filtered-map-container.component.css'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class ServerMapForFilteredMapContainerComponent implements OnInit, OnDestroy {
    private unsubscribe = new Subject<void>();

    i18nText: { [key: string]: string } = {
        NO_AGENTS: ''
    };
    mergedNodeDataList: INodeInfo[] = [];
    mergedLinkDataList: ILinkInfo[] = [];
    mergedServerMapData: any = {};
    mergedScatterData: any;
    loadingCompleted = false;
    mapData: ServerMapData;
    funcServerMapImagePath: Function;
    baseApplicationKey: string;
    showLoading = true;
    useDisable = true;
    isEmpty: boolean;
    endTime: string;
    period: string;

    constructor(
        private router: Router,
        private injector: Injector,
        private componentFactoryResolver: ComponentFactoryResolver,
        private storeHelperService: StoreHelperService,
        private translateService: TranslateService,
        private newUrlStateNotificationService: NewUrlStateNotificationService,
        private serverMapForFilteredMapDataService: ServerMapForFilteredMapDataService,
        private webAppSettingDataService: WebAppSettingDataService,
        private dynamicPopupService: DynamicPopupService,
        private analyticsService: AnalyticsService,
        private cd: ChangeDetectorRef,
        private messageQueueService: MessageQueueService,
        @Inject(SERVER_MAP_TYPE) public type: ServerMapType
    ) {}

    ngOnInit() {
        this.funcServerMapImagePath = this.webAppSettingDataService.getServerMapIconPathMakeFunc();
        this.addPageLoadingHandler();
        this.getI18NText();

        this.newUrlStateNotificationService.onUrlStateChange$.pipe(
            takeUntil(this.unsubscribe)
        ).subscribe((urlService: NewUrlStateNotificationService) => {
            this.endTime = urlService.getPathValue(UrlPathId.END_TIME).getEndTime();
            this.period = urlService.getPathValue(UrlPathId.PERIOD).getValueWithTime();
            this.showLoading = true;
            this.useDisable = true;
            this.baseApplicationKey = urlService.getPathValue(UrlPathId.APPLICATION).getKeyStr();
            this.serverMapForFilteredMapDataService.startDataLoad();
        });
        this.serverMapForFilteredMapDataService.onServerMapData$.pipe(
            takeUntil(this.unsubscribe)
        ).subscribe((serverMapAndScatterData: any) => {
            if (!isEmpty(serverMapAndScatterData.applicationScatterData)) {
                this.storeHelperService.dispatch(new Actions.AddScatterChartData(serverMapAndScatterData.applicationScatterData));
            }

            this.mergeServerMapData(serverMapAndScatterData);
            this.mapData = new ServerMapData(
                this.mergedNodeDataList,
                this.mergedLinkDataList,
                Filter.instanceFromString(this.newUrlStateNotificationService.hasValue(UrlQuery.FILTER) ? this.newUrlStateNotificationService.getPathValue(UrlQuery.FILTER) : '')
            );
            this.isEmpty = this.mapData.getNodeCount() === 0;
            this.messageQueueService.sendMessage({
                to: MESSAGE_TO.SERVER_MAP_DATA_UPDATE,
                param: this.mapData
            });
            if (this.loadingCompleted && this.isEmpty) {
                this.showLoading = false;
                this.useDisable = false;
            }

            this.cd.detectChanges();
        });
        this.connectStore();
        this.messageQueueService.receiveMessage(this.unsubscribe, MESSAGE_TO.SERVER_MAP_DISABLE).subscribe((disable: boolean) => {
            this.useDisable = disable;
            this.cd.detectChanges();
        });
    }

    ngOnDestroy() {
        this.unsubscribe.next();
        this.unsubscribe.complete();
    }

    private connectStore(): void {
        this.storeHelperService.getServerMapLoadingState(this.unsubscribe).subscribe((state: string) => {
            switch (state) {
                case 'loading':
                    this.loadingCompleted = false;
                    this.showLoading = true;
                    this.useDisable = true;
                    break;
                case 'pause':
                    this.loadingCompleted = false;
                    this.showLoading = false;
                    this.useDisable = false;
                    break;
                case 'completed':
                    this.loadingCompleted = true;
                    break;
            }

            this.cd.detectChanges();
        });
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

    private getI18NText(): void {
        this.translateService.get('COMMON.NO_AGENTS').subscribe((i18n: string) => {
            this.i18nText['NO_AGENTS'] = i18n;
        });
    }

    onRenderCompleted(): void {
        if (!this.loadingCompleted) {
            return;
        }

        this.showLoading = false;
        this.useDisable = false;
        this.cd.detectChanges();
    }

    onClickNode(nodeData: any): void {
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.CLICK_NODE);
        let payload;
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
        } else {
            payload = {
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
        this.messageQueueService.sendMessage({
            to: MESSAGE_TO.SERVER_MAP_TARGET_SELECT,
            param: payload
        });
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
                isWAS: false,
                node: [linkData.from],
                link: linkData.targetInfo.map((linkInfo: any) => {
                    return linkInfo.key;
                })
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
                link: [linkData.key]
            };
        }
        this.messageQueueService.sendMessage({
            to: MESSAGE_TO.SERVER_MAP_TARGET_SELECT,
            param: payload
        });
    }

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

    mergeServerMapData(serverMapAndScatterData: any): void {
        const newNodeDataList = serverMapAndScatterData.applicationMapData.nodeDataArray;
        const newLinkDataList = serverMapAndScatterData.applicationMapData.linkDataArray;

        if (this.mergedNodeDataList.length === 0) {
            this.mergedNodeDataList = newNodeDataList;
        } else {
            this.mergeNodeDataList(newNodeDataList);
        }
        if (this.mergedLinkDataList.length === 0) {
            this.mergedLinkDataList = newLinkDataList;
        } else {
            this.mergeLinkDataList(newLinkDataList);
        }
    }

    mergeNodeDataList(newNodeData: INodeInfo[]): void {
        newNodeData.forEach((nodeData: INodeInfo) => {
            if (this.mapData && this.mapData.getNodeData(nodeData.key)) {
                const currentNodeData = this.mapData.getNodeData(nodeData.key) as INodeInfo;
                MergeServerMapData.mergeNodeData(currentNodeData, nodeData);
            } else {
                this.mergedNodeDataList.push(nodeData);
            }
        });
    }

    mergeLinkDataList(newLinkData: ILinkInfo[]): void {
        newLinkData.forEach((linkData: ILinkInfo) => {
            if (this.mapData && this.mapData.getLinkData(linkData.key)) {
                const currentLinkData = this.mapData.getLinkData(linkData.key) as ILinkInfo;
                MergeServerMapData.mergeLinkData(currentLinkData, linkData);
            } else {
                this.mergedLinkDataList.push(linkData);
            }
        });
    }
}
