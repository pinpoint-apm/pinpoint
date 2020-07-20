import { Component, OnInit, OnDestroy, Input, Inject, ChangeDetectionStrategy, ChangeDetectorRef, Injector, ComponentFactoryResolver } from '@angular/core';
import { Subject } from 'rxjs';
import { takeUntil, filter, skip } from 'rxjs/operators';

import { WebAppSettingDataService, GutterEventService, DynamicPopupService, AnalyticsService, TRACKED_EVENT_LIST, MessageQueueService, MESSAGE_TO } from 'app/shared/services';
import { ServerMapInteractionService } from './server-map-interaction.service';
import { ServerMapData } from './class/server-map-data.class';
import { SERVER_MAP_TYPE, ServerMapType } from './class/server-map-factory';
import { ServerMapContextPopupContainerComponent } from 'app/core/components/server-map-context-popup/server-map-context-popup-container.component';
import { ServerMapChangeNotificationService, IServerMapNotificationData } from './server-map-change-notification.service';

@Component({
    selector: 'pp-server-map-others-container',
    templateUrl: './server-map-others-container.component.html',
    styleUrls: ['./server-map-others-container.component.css'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class ServerMapOthersContainerComponent implements OnInit, OnDestroy {
    @Input() sourceType: string;
    private unsubscribe: Subject<null> = new Subject();
    baseApplicationKey: string;
    mapData: ServerMapData;
    showLoading = true;
    funcServerMapImagePath: Function;
    constructor(
        private cd: ChangeDetectorRef,
        private injector: Injector,
        private componentFactoryResolver: ComponentFactoryResolver,
        private webAppSettingDataService: WebAppSettingDataService,
        private dynamicPopupService: DynamicPopupService,
        private gutterEventService: GutterEventService,
        private serverMapInteractionService: ServerMapInteractionService,
        private serverMapChangeNotificationService: ServerMapChangeNotificationService,
        private analyticsService: AnalyticsService,
        private messageQueueService: MessageQueueService,
        @Inject(SERVER_MAP_TYPE) public type: ServerMapType
    ) {}
    ngOnInit() {
        this.funcServerMapImagePath = this.webAppSettingDataService.getServerMapIconPathMakeFunc();
        this.gutterEventService.onGutterResized$.pipe(
            skip(1),
            takeUntil(this.unsubscribe)
        ).subscribe(() => this.serverMapInteractionService.setRefresh());
        this.serverMapChangeNotificationService.getObservable(this.sourceType).pipe(
            takeUntil(this.unsubscribe),
            filter((_: IServerMapNotificationData) => {
                return _ !== null;
            })
        ).subscribe((data: IServerMapNotificationData) => {
            this.baseApplicationKey = data.baseApplication;
            this.mapData = data.serverMapData;
            this.cd.detectChanges();
        });

        this.messageQueueService.receiveMessage(this.unsubscribe, MESSAGE_TO.SERVER_MAP_MERGE_STATE_CHANGE).subscribe((mergeState: IServerMapMergeState) => {
            this.mapData = new ServerMapData(this.mapData.getOriginalNodeList(), this.mapData.getOriginalLinkList(), {...this.mapData.getMergeState(), ...mergeState});
            this.cd.detectChanges();
        });
    }
    ngOnDestroy() {
        this.unsubscribe.next();
        this.unsubscribe.complete();
    }
    onRenderCompleted(msg: string): void {
        this.showLoading = false;
        this.cd.detectChanges();
    }
    onClickNode($event: any): void {}
    onClickLink($event: any): void {}
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
    onContextClickLink($param: any): void {}
}
