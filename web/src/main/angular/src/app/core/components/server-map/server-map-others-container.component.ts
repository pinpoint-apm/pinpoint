import { Component, OnInit, OnDestroy, Input, Inject, ChangeDetectionStrategy, ChangeDetectorRef, Injector, ComponentFactoryResolver } from '@angular/core';
import { Subject } from 'rxjs';
import { takeUntil, filter, skip } from 'rxjs/operators';

import { WebAppSettingDataService, GutterEventService, DynamicPopupService } from 'app/shared/services';
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
        private changeDetectorRef: ChangeDetectorRef,
        private injector: Injector,
        private componentFactoryResolver: ComponentFactoryResolver,
        private webAppSettingDataService: WebAppSettingDataService,
        private dynamicPopupService: DynamicPopupService,
        private gutterEventService: GutterEventService,
        private serverMapInteractionService: ServerMapInteractionService,
        private serverMapChangeNotificationService: ServerMapChangeNotificationService,
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
            this.changeDetectorRef.detectChanges();
        });
    }
    ngOnDestroy() {
        this.unsubscribe.next();
        this.unsubscribe.complete();
    }
    onRenderCompleted(msg: string): void {
        this.showLoading = false;
        this.changeDetectorRef.detectChanges();
    }
    onClickNode($event: any): void {}
    onClickLink($event: any): void {}
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
    onContextClickLink($param: any): void {}
}
