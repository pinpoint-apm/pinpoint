import { Component, OnInit, OnDestroy, HostBinding, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { Subject } from 'rxjs';
import { filter } from 'rxjs/operators';

import { Actions } from 'app/shared/store';
import { WebAppSettingDataService, StoreHelperService, AnalyticsService, TRACKED_EVENT_LIST, MessageQueueService, MESSAGE_TO } from 'app/shared/services';
import { ServerMapData } from 'app/core/components/server-map/class/server-map-data.class';

interface IAppData {
    applicationName: string;
    serviceType: string;
    agentList?: string[];
}

@Component({
    selector: 'pp-side-bar-title-container',
    templateUrl: './side-bar-title-container.component.html',
    styleUrls: ['./side-bar-title-container.component.css'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class SideBarTitleContainerComponent implements OnInit, OnDestroy {
    @HostBinding('class.flex-container') flexContainerClass = true;
    @HostBinding('class.flex-row') flexRowClass = true;

    private static AGENT_ALL = 'All';
    private unsubscribe = new Subject<void>();

    isWAS: boolean;
    isNode: boolean;
    fromAppData: IAppData = null;
    toAppData: IAppData = null;
    selectedAgent = SideBarTitleContainerComponent.AGENT_ALL;
    selectedTarget: ISelectedTarget;
    originalTargetSelected = true;
    serverMapData: any;
    funcImagePath: Function;

    constructor(
        private storeHelperService: StoreHelperService,
        private webAppSettingDataService: WebAppSettingDataService,
        private analyticsService: AnalyticsService,
        private messageQueueService: MessageQueueService,
        private cd: ChangeDetectorRef,
    ) {}

    ngOnInit() {
        this.funcImagePath = this.webAppSettingDataService.getIconPathMakeFunc();
        this.listenToEmitter();
    }

    ngOnDestroy() {
        this.unsubscribe.next();
        this.unsubscribe.complete();
    }

    private listenToEmitter(): void {
        this.messageQueueService.receiveMessage(this.unsubscribe, MESSAGE_TO.SERVER_MAP_DATA_UPDATE).subscribe((data: ServerMapData) => {
            this.serverMapData = data;
        });

        this.messageQueueService.receiveMessage(this.unsubscribe, MESSAGE_TO.SERVER_MAP_TARGET_SELECT).subscribe((target: ISelectedTarget) => {
            this.originalTargetSelected = true;
            this.selectedTarget = target;
            this.onChangeAgent(SideBarTitleContainerComponent.AGENT_ALL);
            this.makeFromToData();
            this.cd.detectChanges();
        });

        this.messageQueueService.receiveMessage(this.unsubscribe, MESSAGE_TO.SERVER_MAP_TARGET_SELECT_BY_LIST).pipe(
            filter(() => this.selectedTarget && this.selectedTarget.isNode && !this.selectedTarget.isMerged)
        ).subscribe((target: any) => {
            this.originalTargetSelected = this.selectedTarget.node[0] === target.key;
            this.cd.detectChanges();
        });
    }

    makeFromToData() {
        if (this.selectedTarget.isNode) {
            this.isWAS = this.selectedTarget.isWAS;
            this.isNode = true;
            const node = this.serverMapData.getNodeData(this.selectedTarget.node[0]);
            this.toAppData = this.formatToAppData({ node: node });
        } else if (this.selectedTarget.isLink) {
            this.isWAS = false;
            this.isNode = false;
            const link = this.serverMapData.getLinkData(this.selectedTarget.link[0]);
            this.fromAppData = this.formatFromAppData(link);
            this.toAppData = this.formatToAppData({ link: link });
        }
    }

    private isUserType(type: string): boolean {
        return type.toUpperCase() === 'USER';
    }

    private formatFromAppData(link: any): IAppData {
        if (this.selectedTarget.isSourceMerge) {
            return {
                applicationName: `[ ${this.selectedTarget.link.length} ] ${link.sourceInfo.serviceType} GROUP`,
                serviceType: link.sourceInfo.serviceType
            };
        } else {
            return {
                applicationName: this.isUserType(link.sourceInfo.serviceType) ? link.sourceInfo.serviceType : link.sourceInfo.applicationName,
                serviceType: link.sourceInfo.serviceType
            };
        }
    }

    private formatToAppData({ node, link }: { node?: any, link?: any }): IAppData {
        if (this.isNode) {
            if (this.selectedTarget.isMerged) {
                return {
                    applicationName: `[ ${this.selectedTarget.node.length} ] ${node.serviceType} GROUP`,
                    serviceType: node.serviceType,
                    agentList: []
                };
            } else {
                return {
                    applicationName: node.applicationName,
                    serviceType: node.serviceType,
                    agentList: [SideBarTitleContainerComponent.AGENT_ALL].concat(node.agentIds.sort())
                };
            }
        } else {
            if (this.selectedTarget.isMerged) {
                if (this.selectedTarget.isSourceMerge) {
                    return {
                        applicationName: link.targetInfo.applicationName,
                        serviceType: link.targetInfo.serviceType,
                        agentList: []
                    };

                } else {
                    return {
                        applicationName: `[ ${this.selectedTarget.link.length} ] ${link.targetInfo.serviceType} GROUP`,
                        serviceType: link.targetInfo.serviceType,
                        agentList: []
                    };
                }
            } else {
                return {
                    applicationName: link.targetInfo.applicationName,
                    serviceType: link.targetInfo.serviceType,
                    agentList: []
                };
            }
        }
    }

    onChangeAgent(agentName: string): void {
        this.selectedAgent = agentName;
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.SELECT_AGENT);
        this.storeHelperService.dispatch(new Actions.ChangeAgent(agentName === SideBarTitleContainerComponent.AGENT_ALL ? '' : agentName));
    }
}
