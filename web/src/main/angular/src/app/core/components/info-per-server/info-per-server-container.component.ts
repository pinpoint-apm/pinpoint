import { Component, OnInit, OnDestroy, ComponentFactoryResolver, Injector, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { trigger, state, style, animate, transition } from '@angular/animations';
import { EMPTY, Subject } from 'rxjs';
import { catchError, filter, map, switchMap, tap } from 'rxjs/operators';

import {
    StoreHelperService,
    NewUrlStateNotificationService,
    UrlRouteManagerService,
    AgentHistogramDataService,
    DynamicPopupService,
    AnalyticsService,
    TRACKED_EVENT_LIST,
    MessageQueueService,
    MESSAGE_TO
} from 'app/shared/services';
import { Actions } from 'app/shared/store/reducers';
import { ServerMapData } from 'app/core/components/server-map/class/server-map-data.class';
import { ServerErrorPopupContainerComponent } from 'app/core/components/server-error-popup/server-error-popup-container.component';

@Component({
    selector: 'pp-info-per-server-container',
    templateUrl: './info-per-server-container.component.html',
    styleUrls: ['./info-per-server-container.component.css'],
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
    ],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class InfoPerServerContainerComponent implements OnInit, OnDestroy {
    private unsubscribe = new Subject<void>();

    selectedTarget: ISelectedTarget;
    serverMapData: ServerMapData;
    agentHistogramData: any;
    selectedAgent: string;
    selectedAgentName: string;
    listAnimationTrigger = 'start';
    chartAnimationTrigger = 'start';

    constructor(
        private storeHelperService: StoreHelperService,
        private newUrlStateNotificationService: NewUrlStateNotificationService,
        private urlRouteManagerService: UrlRouteManagerService,
        private agentHistogramDataService: AgentHistogramDataService,
        private dynamicPopupService: DynamicPopupService,
        private analyticsService: AnalyticsService,
        private componentFactoryResolver: ComponentFactoryResolver,
        private messageQueueService: MessageQueueService,
        private injector: Injector,
        private cd: ChangeDetectorRef,
    ) {}

    ngOnInit() {
        this.listenToEmitter();
    }

    ngOnDestroy() {
        this.unsubscribe.next();
        this.unsubscribe.complete();
    }

    private listenToEmitter(): void {
        this.messageQueueService.receiveMessage(this.unsubscribe, MESSAGE_TO.SERVER_MAP_DATA_UPDATE).subscribe(({serverMapData}: {serverMapData: ServerMapData}) => {
            this.serverMapData = serverMapData;
        });

        this.messageQueueService.receiveMessage(this.unsubscribe, MESSAGE_TO.SERVER_MAP_TARGET_SELECT).subscribe((target: ISelectedTarget) => {
            this.selectedTarget = target;
            this.selectedAgent = '';
            this.selectedAgentName = null;
            this.cd.detectChanges();
        });

        this.storeHelperService.getInfoPerServerState(this.unsubscribe).pipe(
            filter(() => this.selectedTarget && this.selectedTarget.isNode),
            filter((visibleState: boolean) => visibleState ? true : (this.hide(), this.cd.detectChanges(), false)),
            map(() => this.serverMapData.getNodeData(this.selectedTarget.node[0])),
            switchMap((node: INodeInfo) => {
                const range = [
                    this.newUrlStateNotificationService.getStartTimeToNumber(),
                    this.newUrlStateNotificationService.getEndTimeToNumber()
                ];

                return this.agentHistogramDataService.getData(this.serverMapData, range, node).pipe(
                    tap((histogramData = {}) => {
                        this.agentHistogramData = { isWas: node.isWas, ...histogramData };
                        this.selectedAgent = this.selectedAgent ? this.selectedAgent : this.getFirstAgent();
                        this.selectedAgentName = this.getAgentName(this.selectedAgent);
                        this.messageQueueService.sendMessage({
                            to: MESSAGE_TO.AGENT_SELECT_FOR_SERVER_LIST,
                            param: {
                                agent: this.selectedAgent,
                                responseSummary: this.agentHistogramData['agentHistogram'][this.selectedAgent],
                                load: this.agentHistogramData['agentTimeSeriesHistogram'][this.selectedAgent],
                                responseStatistics: this.agentHistogramData['agentResponseStatistics'][this.selectedAgent]
                            }
                        });
                    }),
                    catchError((error: IServerError) => {
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

                        this.messageQueueService.sendMessage({
                            to: MESSAGE_TO.SERVER_MAP_DISABLE,
                            param: false
                        });

                        return EMPTY;
                    })
                );
            })
        ).subscribe(() => {
            this.show();
            this.cd.detectChanges();
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

    private getFirstAgent(): string {
        const firstKey = Object.keys(this.agentHistogramData['serverList']).sort()[0];

        return Object.keys(this.agentHistogramData['serverList'][firstKey]['instanceList']).sort()[0];
    }

    private getAgentName(agentId: string): string {
        const serverList = Object.keys(this.agentHistogramData['serverList']);
        for (let server of serverList) {
            const agentIds = Object.keys(this.agentHistogramData['serverList'][server]['instanceList']);
            if (agentIds && agentIds.includes(agentId)) {
                return this.agentHistogramData['serverList'][server]['instanceList'][agentId]['agentName'];
            }
        }
        return null;
    }

    onSelectAgent(agent: string): void {
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.SELECT_AGENT_ON_SERVER_LIST_VIEW);
        this.messageQueueService.sendMessage({
            to: MESSAGE_TO.AGENT_SELECT_FOR_SERVER_LIST,
            param: {
                agent,
                responseSummary: this.agentHistogramData['agentHistogram'][agent],
                load: this.agentHistogramData['agentTimeSeriesHistogram'][agent],
                responseStatistics: this.agentHistogramData['agentResponseStatistics'][agent]
            }
        });
        this.selectedAgent = agent;
        this.selectedAgentName = this.getAgentName(agent);
    }

    onOpenInspector(agent: string): void {
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.OPEN_INSPECTOR_WITH_AGENT);
        this.urlRouteManagerService.openInspectorPage(false, this.selectedTarget.node[0].replace('^', '@'), agent);
    }
}
