import { Component, OnInit, OnDestroy, HostBinding, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { Subject } from 'rxjs';
import { filter } from 'rxjs/operators';

import { Actions } from 'app/shared/store';
import { WebAppSettingDataService, StoreHelperService, AnalyticsService, TRACKED_EVENT_LIST } from 'app/shared/services';

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
    isWAS: boolean;
    isNode: boolean;
    fromAppData: IAppData = null;
    toAppData: IAppData = null;
    selectedTarget: ISelectedTarget;
    serverMapData: any;
    funcImagePath: Function;
    unsubscribe: Subject<null> = new Subject();
    constructor(
        private changeDetector: ChangeDetectorRef,
        private storeHelperService: StoreHelperService,
        private webAppSettingDataService: WebAppSettingDataService,
        private analyticsService: AnalyticsService,
    ) {
    }
    ngOnInit() {
        this.funcImagePath = this.webAppSettingDataService.getIconPathMakeFunc();
        this.connectStore();
    }
    ngOnDestroy() {
        this.unsubscribe.next();
        this.unsubscribe.complete();
    }
    private connectStore(): void {
        this.storeHelperService.getServerMapData(this.unsubscribe).subscribe((serverMapData: IServerMapInfo) => {
            this.serverMapData = serverMapData;
        });
        this.storeHelperService.getServerMapTargetSelected(this.unsubscribe).pipe(
            filter((target: ISelectedTarget) => {
                return target && (target.isNode === true || target.isNode === false) ? true : false;
            })
        ).subscribe((target: ISelectedTarget) => {
            if ( target.isNode || target.isLink ) {
                this.selectedTarget = target;
                this.makeFromToData();
                this.changeDetector.detectChanges();
            }
        });
    }
    makeFromToData() {
        if ( this.selectedTarget.isNode ) {
            this.isWAS = this.selectedTarget.isWAS;
            this.isNode = true;
            const node = this.serverMapData.getNodeData(this.selectedTarget.node[0]);
            this.toAppData = this.formatToAppData({ node: node });
        } else if ( this.selectedTarget.isLink ) {
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
        return {
            applicationName: this.isUserType(link.sourceInfo.serviceType) ? link.sourceInfo.serviceType : link.sourceInfo.applicationName,
            serviceType: link.sourceInfo.serviceType
        };
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
                    agentList: node.agentIds.sort()
                };
            }
        } else {
            if (this.selectedTarget.isMerged) {
                return {
                    applicationName: `[ ${this.selectedTarget.link.length} ] ${link.targetInfo.serviceType} GROUP`,
                    serviceType: link.targetInfo.serviceType,
                    agentList: []
                };
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
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.SELECT_AGENT);
        agentName = agentName === 'All' ? '' : agentName;
        this.storeHelperService.dispatch(new Actions.ChangeAgent(agentName));
    }
}
