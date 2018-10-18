import { Component, OnInit, Inject, ChangeDetectionStrategy } from '@angular/core';
import { Subject, Observable } from 'rxjs';
import { switchMap, map, takeUntil } from 'rxjs/operators';

import { UrlPathId } from 'app/shared/models';
import { NewUrlStateNotificationService, WebAppSettingDataService, TransactionDetailDataService, GutterEventService } from 'app/shared/services';
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
    baseApplicationKey = '';
    mapData$: Observable<ServerMapData>;
    funcServerMapImagePath: Function;
    showLoading = true;
    constructor(
        private newUrlStateNotificationService: NewUrlStateNotificationService,
        private webAppSettingDataService: WebAppSettingDataService,
        private transactionDetailDataService: TransactionDetailDataService,
        private gutterEventService: GutterEventService,
        private serverMapInteractionService: ServerMapInteractionService,
        @Inject(SERVER_MAP_TYPE) public type: ServerMapType
    ) { }

    ngOnInit() {
        this.funcServerMapImagePath = this.webAppSettingDataService.getServerMapIconPathMakeFunc();
        // TODO: ServiceType Empty이슈 체크 #174
        this.mapData$ = this.newUrlStateNotificationService.onUrlStateChange$.pipe(
            switchMap((urlService: NewUrlStateNotificationService) => this.transactionDetailDataService.getData(
                urlService.getPathValue(UrlPathId.AGENT_ID),
                urlService.getPathValue(UrlPathId.SPAN_ID),
                urlService.getPathValue(UrlPathId.TRACE_ID),
                urlService.getPathValue(UrlPathId.FOCUS_TIMESTAMP)
            )),
            map((applicationMapData: ITransactionDetailData) => {
                return new ServerMapData(applicationMapData.applicationMapData.nodeDataArray, applicationMapData.applicationMapData.linkDataArray);
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
