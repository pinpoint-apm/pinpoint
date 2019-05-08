import { Component, OnInit, Inject, ChangeDetectionStrategy, ChangeDetectorRef, Injector, ComponentFactoryResolver } from '@angular/core';
import { Subject } from 'rxjs';
import { map, takeUntil, skip } from 'rxjs/operators';

import { WebAppSettingDataService, GutterEventService, StoreHelperService, DynamicPopupService } from 'app/shared/services';
import { ServerMapInteractionService } from './server-map-interaction.service';
import { ServerMapData } from './class/server-map-data.class';
import { SERVER_MAP_TYPE, ServerMapType } from './class/server-map-factory';
import { ServerMapContextPopupContainerComponent } from 'app/core/components/server-map-context-popup/server-map-context-popup-container.component';

@Component({
    selector: 'pp-server-map-for-transaction-view-container',
    templateUrl: './server-map-for-transaction-view-container.component.html',
    styleUrls: ['./server-map-for-transaction-view-container.component.css'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class ServerMapForTransactionViewContainerComponent implements OnInit {
    private unsubscribe: Subject<void> = new Subject<void>();
    baseApplicationKey: string;
    mapData: ServerMapData;
    funcServerMapImagePath: Function;
    showLoading = true;
    constructor(
        private changeDetectorRef: ChangeDetectorRef,
        private injector: Injector,
        private componentFactoryResolver: ComponentFactoryResolver,
        private webAppSettingDataService: WebAppSettingDataService,
        private dynamicPopupService: DynamicPopupService,
        private gutterEventService: GutterEventService,
        private serverMapInteractionService: ServerMapInteractionService,
        private storeHelperService: StoreHelperService,
        @Inject(SERVER_MAP_TYPE) public type: ServerMapType
    ) { }

    ngOnInit() {
        this.funcServerMapImagePath = this.webAppSettingDataService.getServerMapIconPathMakeFunc();
        // TODO: ServiceType Empty이슈 체크 #174
        this.storeHelperService.getTransactionDetailData(this.unsubscribe).pipe(
            skip(1),
            map(({ applicationId, applicationMapData: { nodeDataArray, linkDataArray } }: ITransactionDetailData) => {
                this.baseApplicationKey = (nodeDataArray as INodeInfo[]).find(({ applicationName }: INodeInfo) => {
                    return applicationId === applicationName;
                }).key;

                return new ServerMapData(nodeDataArray, linkDataArray);
            })
        ).subscribe((mapData: ServerMapData) => {
            console.log( 'mapData : ', mapData );
            this.mapData = mapData;
            this.changeDetectorRef.detectChanges();
        });
        this.gutterEventService.onGutterResized$.pipe(
            takeUntil(this.unsubscribe)
        ).subscribe(() => this.serverMapInteractionService.setRefresh());
    }
    onRenderCompleted(msg: string): void {
        this.showLoading = false;
        this.changeDetectorRef.detectChanges();
    }
    onClickBackground($event: any): void {}
    onClickGroupNode($event: any): void {}
    onClickNode($event: any): void {}
    onClickLink($event: any): void {}
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
    onContextClickLink($param: any): void {}
}
