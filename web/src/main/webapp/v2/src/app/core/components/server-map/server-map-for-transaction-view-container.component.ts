import { Component, OnInit, Inject, ChangeDetectionStrategy } from '@angular/core';
import { Subject, Observable } from 'rxjs';
import { map, takeUntil, skip } from 'rxjs/operators';

import { WebAppSettingDataService, GutterEventService, StoreHelperService } from 'app/shared/services';
import { ServerMapInteractionService } from './server-map-interaction.service';
import { ServerMapData } from './class/server-map-data.class';
import { SERVER_MAP_TYPE, ServerMapType } from './class/server-map-factory';

@Component({
    selector: 'pp-server-map-for-transaction-view-container',
    templateUrl: './server-map-for-transaction-view-container.component.html',
    styleUrls: ['./server-map-for-transaction-view-container.component.css'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class ServerMapForTransactionViewContainerComponent implements OnInit {
    private unsubscribe: Subject<void> = new Subject<void>();
    baseApplicationKey: string;
    mapData$: Observable<ServerMapData>;
    funcServerMapImagePath: Function;
    showLoading = true;
    constructor(
        private webAppSettingDataService: WebAppSettingDataService,
        private gutterEventService: GutterEventService,
        private serverMapInteractionService: ServerMapInteractionService,
        private storeHelperService: StoreHelperService,
        @Inject(SERVER_MAP_TYPE) public type: ServerMapType
    ) { }

    ngOnInit() {
        this.funcServerMapImagePath = this.webAppSettingDataService.getServerMapIconPathMakeFunc();
        // TODO: ServiceType Empty이슈 체크 #174
        this.mapData$ = this.storeHelperService.getTransactionDetailData(this.unsubscribe).pipe(
            skip(1),
            map(({ applicationId, applicationMapData: { nodeDataArray, linkDataArray } }: ITransactionDetailData) => {
                this.baseApplicationKey = (nodeDataArray as INodeInfo[]).find(({ applicationName }: INodeInfo) => {
                    return applicationId === applicationName;
                }).key;

                return new ServerMapData(nodeDataArray, linkDataArray);
            })
        );
        this.gutterEventService.onGutterResized$.pipe(
            takeUntil(this.unsubscribe)
        ).subscribe(() => this.serverMapInteractionService.setRefresh());
    }
    onRenderCompleted(msg: string): void {
        this.showLoading = false;
    }
    onClickBackground($event: any): void {}
    onClickGroupNode($event: any): void {}
    onClickNode($event: any): void {}
    onClickLink($event: any): void {}
    onDoubleClickBackground($event: any): void {}
    onContextClickBackground($event: any): void {}
    onContextClickNode($event: any): void {}
    onContextClickLink($param: any): void {}
}
