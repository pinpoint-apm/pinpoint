import { Component, OnInit, OnDestroy, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { trigger, state, style, animate, transition } from '@angular/animations';
import { Subject } from 'rxjs';

import {
    StoreHelperService,
    NewUrlStateNotificationService,
    UrlRouteManagerService,
    AnalyticsService,
    TRACKED_EVENT_LIST
} from 'app/shared/services';
import { Actions } from 'app/shared/store';
import { ServerMapData } from 'app/core/components/server-map/class/server-map-data.class';

@Component({
    selector: 'pp-info-per-server-for-filtered-map-container',
    templateUrl: './info-per-server-for-filtered-map-container.component.html',
    styleUrls: ['./info-per-server-for-filtered-map-container.component.css'],
    changeDetection: ChangeDetectionStrategy.OnPush,
    animations: [
        trigger('listAnimationTrigger', [
            state('start', style({
                left: '0px'
            })),
            state('end', style({
                left: '-825px'
            })),
            transition('* => *', [
                animate('0.2s 0s ease-out')
            ])
        ]),
        trigger('chartAnimationTrigger', [
            state('start', style({
                left: '0px'
            })),
            state('end', style({
                left: '-477px'
            })),
            transition('* => *', [
                animate('0.2s 0s ease-out')
            ])
        ]),
    ]
})
export class InfoPerServerForFilteredMapContainerComponent implements OnInit, OnDestroy {
    private unsubscribe: Subject<null> = new Subject();
    selectedTarget: ISelectedTarget;
    serverMapData: ServerMapData;
    agentHistogramData: any;
    selectedAgent = '';
    listAnimationTrigger = 'start';
    chartAnimationTrigger = 'start';
    constructor(
        private changeDetector: ChangeDetectorRef,
        private storeHelperService: StoreHelperService,
        private urlRouteManagerService: UrlRouteManagerService,
        private analyticsService: AnalyticsService,
    ) {}
    ngOnInit() {
        this.connectStore();
    }
    ngOnDestroy() {
        this.unsubscribe.next();
        this.unsubscribe.complete();
    }
    private connectStore(): void {
        this.storeHelperService.getServerMapTargetSelected(this.unsubscribe).subscribe((target: ISelectedTarget) => {
            this.selectedTarget = target;
            this.selectedAgent = '';
        });
        this.storeHelperService.getServerMapData(this.unsubscribe).subscribe((serverMapData: ServerMapData) => {
            this.serverMapData = serverMapData;
        });
        this.storeHelperService.getInfoPerServerState(this.unsubscribe).subscribe((visibleState: boolean) => {
            if (this.selectedTarget && this.selectedTarget.isNode) {
                if (visibleState === true) {
                    const node = this.serverMapData.getNodeData(this.selectedTarget.node[0]);
                    this.show();
                    this.agentHistogramData = {
                        serverList: node.serverList,
                        agentHistogram: node.agentHistogram,
                        agentTimeSeriesHistogram: node.agentTimeSeriesHistogram,
                        isWas: node.isWas
                    };
                    this.changeDetector.detectChanges();
                    this.storeHelperService.dispatch(new Actions.UpdateServerList(this.agentHistogramData));
                    this.onSelectAgent(this.selectedAgent ? this.selectedAgent : this.getFirstAgent());
                    this.storeHelperService.dispatch(new Actions.ChangeInfoPerServerVisibleState(true));
                } else {
                    this.hide();
                    this.changeDetector.detectChanges();
                    this.storeHelperService.dispatch(new Actions.ChangeInfoPerServerVisibleState(false));
                }
            }
        });
    }
    private hide(): void {
        this.listAnimationTrigger = 'start';
        this.chartAnimationTrigger = 'start';
    }
    private show(): void {
        this.listAnimationTrigger = 'end';
        this.chartAnimationTrigger = 'end';
    }
    isWAS(): boolean {
        return this.selectedTarget.isWAS;
    }
    getFirstAgent(): string {
        const firstKey = Object.keys(this.agentHistogramData['serverList']).sort()[0];
        return Object.keys(this.agentHistogramData['serverList'][firstKey]['instanceList']).sort()[0];
    }
    onSelectAgent(agentName: string): void {
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.SELECT_AGENT);
        this.storeHelperService.dispatch(new Actions.ChangeAgentForServerList({
            agent: agentName,
            responseSummary: this.agentHistogramData['agentHistogram'][agentName],
            load: this.agentHistogramData['agentTimeSeriesHistogram'][agentName]
        }));
        this.selectedAgent = agentName;
        this.changeDetector.detectChanges();
    }
    onOpenInspector(agentName: string): void {
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.OPEN_INSPECTOR_WITH_AGENT);
        this.urlRouteManagerService.openInspectorPage(false, agentName);
    }
}
