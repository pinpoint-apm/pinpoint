import { Component, OnInit, OnDestroy, ChangeDetectionStrategy, ChangeDetectorRef, ComponentFactoryResolver, Injector } from '@angular/core';
import { trigger, state, style, animate, transition } from '@angular/animations';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

import {
    StoreHelperService,
    NewUrlStateNotificationService,
    UrlRouteManagerService,
    AgentHistogramDataService,
    DynamicPopupService,
    AnalyticsService,
    TRACKED_EVENT_LIST
} from 'app/shared/services';
import { Actions } from 'app/shared/store';
import { UrlPath, UrlPathId } from 'app/shared/models';
import { ServerMapData } from 'app/core/components/server-map/class/server-map-data.class';
import { ServerErrorPopupContainerComponent } from 'app/core/components/server-error-popup';

@Component({
    selector: 'pp-info-per-server-container',
    templateUrl: './info-per-server-container.component.html',
    styleUrls: ['./info-per-server-container.component.css'],
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
export class InfoPerServerContainerComponent implements OnInit, OnDestroy {
    private unsubscribe: Subject<null> = new Subject();
    selectedTarget: ISelectedTarget;
    serverMapData: ServerMapData;
    agentHistogramData: any;
    selectedAgent = '';
    listAnimationTrigger = 'start';
    chartAnimationTrigger = 'start';
    constructor(
        private storeHelperService: StoreHelperService,
        private changeDetector: ChangeDetectorRef,
        private newUrlStateNotificationService: NewUrlStateNotificationService,
        private urlRouteManagerService: UrlRouteManagerService,
        private agentHistogramDataService: AgentHistogramDataService,
        private dynamicPopupService: DynamicPopupService,
        private analyticsService: AnalyticsService,
        private componentFactoryResolver: ComponentFactoryResolver,
        private injector: Injector
    ) {}
    ngOnInit() {
        this.connectStore();
        this.newUrlStateNotificationService.onUrlStateChange$.pipe(
            takeUntil(this.unsubscribe)
        ).subscribe((urlService: NewUrlStateNotificationService) => {
            this.hide();
            this.changeDetector.detectChanges();
        });
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
                const node = this.serverMapData.getNodeData(this.selectedTarget.node[0]);
                if (visibleState === true) {
                    this.agentHistogramDataService.getData(node.key, node.applicationName, node.serviceTypeCode, this.serverMapData).subscribe((histogramData: any) => {
                        this.show();
                        this.agentHistogramData = histogramData || {};
                        this.agentHistogramData.isWas = node.isWas;
                        this.changeDetector.detectChanges();
                        this.storeHelperService.dispatch(new Actions.UpdateServerList(this.agentHistogramData));
                        this.onSelectAgent(this.selectedAgent ? this.selectedAgent : this.getFirstAgent());
                        this.storeHelperService.dispatch(new Actions.ChangeInfoPerServerVisibleState(true));
                    }, (error: IServerErrorFormat) => {
                        this.dynamicPopupService.openPopup({
                            data: {
                                title: 'Error',
                                contents: error
                            },
                            component: ServerErrorPopupContainerComponent
                        }, {
                            resolver: this.componentFactoryResolver,
                            injector: this.injector
                        });
                        this.storeHelperService.dispatch(new Actions.ChangeServerMapDisableState(false));
                        this.storeHelperService.dispatch(new Actions.ChangeInfoPerServerVisibleState(false));
                    });
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
