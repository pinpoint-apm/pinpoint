import {ChangeDetectorRef, Component, ComponentFactoryResolver, Injector, OnDestroy, OnInit} from '@angular/core';
import {Subject, merge} from 'rxjs';
import {filter, switchMap, tap, map, takeUntil} from 'rxjs/operators';

import {
    AnalyticsService,
    DynamicPopupService,
    MessageQueueService,
    MESSAGE_TO,
    NewUrlStateNotificationService,
    TRACKED_EVENT_LIST
} from 'app/shared/services';
import {IApdexFormulaData, ApdexScoreDataService} from './apdex-score-data.service';
import {ServerMapData} from 'app/core/components/server-map/class/server-map-data.class';
import {
    HELP_VIEWER_LIST,
    HelpViewerPopupContainerComponent
} from 'app/core/components/help-viewer-popup/help-viewer-popup-container.component';
import {ApdexScoreGuideComponent} from './apdex-score-guide.component';
import {ApdexScoreInteractionService} from './apdex-score-interaction.service';

@Component({
    selector: 'pp-apdex-score-container',
    templateUrl: './apdex-score-container.component.html',
    styleUrls: ['./apdex-score-container.component.css']
})
export class ApdexScoreContainerComponent implements OnInit, OnDestroy {
    private unsubscribe = new Subject<void>();

    private selectedTarget: ISelectedTarget;
    private serverMapData: ServerMapData;
    private previousRange: number[];

    apdexScore: number;
    isEmpty: boolean;

    constructor(
        private apdexScoreDataService: ApdexScoreDataService,
        private messageQueueService: MessageQueueService,
        private newUrlStateNotificationService: NewUrlStateNotificationService,
        private cd: ChangeDetectorRef,
        private injector: Injector,
        private analyticsService: AnalyticsService,
        private dynamicPopupService: DynamicPopupService,
        private componentFactoryResolver: ComponentFactoryResolver,
        private apdexScoreInteractionService: ApdexScoreInteractionService
    ) {
    }

    ngOnInit() {
        this.newUrlStateNotificationService.onUrlStateChange$.pipe(
            takeUntil((this.unsubscribe)),
        ).subscribe(() => {
            this.serverMapData = null;
            this.selectedTarget = null;
        });

        merge(
            this.messageQueueService.receiveMessage(this.unsubscribe, MESSAGE_TO.SERVER_MAP_DATA_UPDATE).pipe(
                tap(({serverMapData, range}: { serverMapData: ServerMapData, range: number[] }) => {
                    this.serverMapData = serverMapData;
                    this.previousRange = range;
                }),
                filter(() => !!this.selectedTarget),
            ),
            this.messageQueueService.receiveMessage(this.unsubscribe, MESSAGE_TO.SERVER_MAP_TARGET_SELECT).pipe(
                filter(({isWAS, isMerged}: ISelectedTarget) => isWAS && !isMerged),
                tap((target: ISelectedTarget) => this.selectedTarget = target),
            )
        ).pipe(
            map(() => this.serverMapData.getNodeData(this.selectedTarget.node[0])),
            switchMap(({applicationName, serviceTypeCode}: INodeInfo) => {
                return this.apdexScoreDataService.getApdexScore({
                    applicationName,
                    serviceTypeCode,
                    from: this.previousRange[0],
                    to: this.previousRange[1]
                });
            })
        ).subscribe(({
                         apdexScore,
                         apdexFormula: apdexFormulaData
                     }: { apdexScore: number, apdexFormula: IApdexFormulaData }) => {
            this.apdexScore = apdexScore;
            this.isEmpty = apdexFormulaData.totalSamples === 0;
            this.apdexScoreInteractionService.setApdexFormulaData(apdexFormulaData);
            this.cd.detectChanges();
        });
    }

    ngOnDestroy() {
        this.unsubscribe.next();
        this.unsubscribe.complete();
    }

    onShowHelp($event: MouseEvent): void {
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.CLICK_APDEX_SCORE, HELP_VIEWER_LIST.APDEX_SCORE);
        const {left, top, height} = ($event.target as HTMLElement).getBoundingClientRect();

        this.dynamicPopupService.openPopup({
            data: HELP_VIEWER_LIST.APDEX_SCORE,
            template: ApdexScoreGuideComponent,
            coord: {
                coordX: left,
                coordY: top + height / 2
            },
            component: HelpViewerPopupContainerComponent
        }, {
            resolver: this.componentFactoryResolver,
            injector: this.injector
        });
    }
}
