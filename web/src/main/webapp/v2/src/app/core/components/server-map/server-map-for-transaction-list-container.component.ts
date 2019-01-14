import { Component, OnInit, OnDestroy, Inject, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { Subject } from 'rxjs';
import { takeUntil, filter } from 'rxjs/operators';

import { UrlPathId } from 'app/shared/models';
import { StoreHelperService, NewUrlStateNotificationService, WebAppSettingDataService , TransactionViewTypeService, VIEW_TYPE } from 'app/shared/services';
import { ServerMapData } from './class/server-map-data.class';
import { SERVER_MAP_TYPE, ServerMapType } from './class/server-map-factory';


@Component({
    selector: 'pp-server-map-for-transaction-list-container',
    templateUrl: './server-map-for-transaction-list-container.component.html',
    styleUrls: ['./server-map-for-transaction-list-container.component.css'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class ServerMapForTransactionListContainerComponent implements OnInit, OnDestroy {
    private unsubscribe: Subject<null> = new Subject();
    private transactionDetailInfo: ITransactionDetailData;
    transactionInfo: ITransactionMetaData;
    hiddenComponent = false;
    baseApplicationKey = '';
    mapData: ServerMapData;
    showLoading = true;
    funcServerMapImagePath: Function;
    constructor(
        private changeDetectorRef: ChangeDetectorRef,
        private newUrlStateNotificationService: NewUrlStateNotificationService,
        private storeHelperService: StoreHelperService,
        private webAppSettingDataService: WebAppSettingDataService,
        private transactionViewTypeService: TransactionViewTypeService,
        @Inject(SERVER_MAP_TYPE) public type: ServerMapType
    ) {
        this.funcServerMapImagePath = this.webAppSettingDataService.getServerMapIconPathMakeFunc();
        this.showLoading = false;
    }
    ngOnInit() {
        this.newUrlStateNotificationService.onUrlStateChange$.pipe(
            takeUntil(this.unsubscribe),
            filter((urlService: NewUrlStateNotificationService) => {
                return urlService.hasValue(UrlPathId.APPLICATION);
            })
        ).subscribe((urlService: NewUrlStateNotificationService) => {
            this.baseApplicationKey = urlService.getPathValue(UrlPathId.APPLICATION).getKeyStr();
            this.changeDetectorRef.detectChanges();
        });
        this.transactionViewTypeService.onChangeViewType$.pipe(
            takeUntil(this.unsubscribe)
        ).subscribe((viewType: string) => {
            if ( viewType === VIEW_TYPE.SERVER_MAP ) {
                this.hiddenComponent = false;
                this.initCheck();
            } else {
                this.hiddenComponent = true;
            }
            this.changeDetectorRef.detectChanges();
        });
        this.connectStore();
    }
    ngOnDestroy() {
        this.unsubscribe.next();
        this.unsubscribe.complete();
    }
    private connectStore(): void {
        this.storeHelperService.getTransactionDetailData(this.unsubscribe).pipe(
            filter((transactionDetailInfo: ITransactionDetailData) => {
                return transactionDetailInfo && transactionDetailInfo.transactionId ? true : false;
            })
        ).subscribe((transactionDetailInfo: ITransactionDetailData) => {
            this.mapData = null;
            this.transactionDetailInfo = transactionDetailInfo;
            this.initCheck();
            this.changeDetectorRef.detectChanges();
        });
    }
    private initCheck() {
        if (this.hiddenComponent === false && this.transactionDetailInfo) {
            this.loadTransactionData();
        }
    }
    private loadTransactionData(): void {
        this.mapData = new ServerMapData(this.transactionDetailInfo.applicationMapData.nodeDataArray, this.transactionDetailInfo.applicationMapData.linkDataArray);
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
    onContextClickBackground($event: any): void {}
    onContextClickNode($event: any): void {}
    onContextClickLink($param: any): void {}
}
