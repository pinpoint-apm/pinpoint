import { Component, OnInit, OnDestroy, ChangeDetectionStrategy, ChangeDetectorRef, ComponentFactoryResolver, Injector } from '@angular/core';
import { Subject } from 'rxjs';
import { filter, takeUntil } from 'rxjs/operators';

import {
    StoreHelperService,
    NewUrlStateNotificationService,
    UrlRouteManagerService,
    TransactionViewTypeService,
    IViewType, TransactionDetailDataService,
    ITransactionDetailPartInfo,
    AnalyticsService,
    TRACKED_EVENT_LIST,
    DynamicPopupService
} from 'app/shared/services';
import { UrlPath, UrlPathId } from 'app/shared/models';
import { MessagePopupContainerComponent } from 'app/core/components/message-popup/message-popup-container.component';

@Component({
    selector: 'pp-transaction-detail-menu-container',
    templateUrl: './transaction-detail-menu-container.component.html',
    styleUrls: ['./transaction-detail-menu-container.component.css'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class TransactionDetailMenuContainerComponent implements OnInit, OnDestroy {
    private unsubscribe: Subject<void> = new Subject();
    private transactionInfo: ITransactionMetaData;
    viewTypeList: IViewType[];
    viewType: string;
    partInfo: ITransactionDetailPartInfo;
    constructor(
        private changeDetectorRef: ChangeDetectorRef,
        private storeHelperService: StoreHelperService,
        private newUrlStateNotificationService: NewUrlStateNotificationService,
        private urlRouteManagerService: UrlRouteManagerService,
        private transactionViewTypeService: TransactionViewTypeService,
        private transactionDetailDataService: TransactionDetailDataService,
        private analyticsService: AnalyticsService,
        private dynamicPopupService: DynamicPopupService,
        private componentFactoryResolver: ComponentFactoryResolver,
        private injector: Injector
    ) {}
    ngOnInit() {
        this.viewTypeList = this.transactionViewTypeService.getViewTypeList();
        this.transactionViewTypeService.onChangeViewType$.pipe(
            takeUntil(this.unsubscribe)
        ).subscribe((viewType: string) => {
            this.viewType = viewType;
            this.changeDetectorRef.detectChanges();
        });
        this.transactionDetailDataService.partInfo$.pipe(
            takeUntil(this.unsubscribe)
        ).subscribe((partInfo: ITransactionDetailPartInfo) => {
            this.partInfo = partInfo;
            this.changeDetectorRef.detectChanges();
        });
        this.connectStore();
    }
    ngOnDestroy() {
        this.unsubscribe.next();
        this.unsubscribe.complete();
    }
    private connectStore(): void {
        this.storeHelperService.getTransactionData(this.unsubscribe).pipe(
            filter((data: ITransactionMetaData) => {
                if (data && data.application && data.agentId && data.traceId) {
                    return true;
                }
                return false;
            })
        ).subscribe((transactionInfo: ITransactionMetaData) => {
            this.transactionInfo = transactionInfo;
            this.changeDetectorRef.detectChanges();
        });
    }
    onSelectViewType(viewType: string): void {
        this.analyticsService.trackEvent((TRACKED_EVENT_LIST as any)[`CLICK_${viewType}`]);
        this.urlRouteManagerService.moveOnPage({
            url: [
                UrlPath.TRANSACTION_LIST,
                this.newUrlStateNotificationService.getPathValue(UrlPathId.APPLICATION).getUrlStr(),
                this.newUrlStateNotificationService.getPathValue(UrlPathId.PERIOD).getValueWithTime(),
                this.newUrlStateNotificationService.getPathValue(UrlPathId.END_TIME).getEndTime(),
                this.newUrlStateNotificationService.getPathValue(UrlPathId.TRANSACTION_INFO),
                viewType
            ]
        });
    }
    onOpenDetailView(): void {
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.OPEN_TRANSACTION_VIEW);
        this.urlRouteManagerService.openPage([
            UrlPath.TRANSACTION_VIEW,
            this.transactionInfo.agentId,
            this.transactionInfo.traceId,
            this.transactionInfo.collectorAcceptTime + '',
            this.transactionInfo.spanId,
        ]);
    }
    onOpenExtraView(param: any): void {
        if (param.open) {
            this.urlRouteManagerService.openPage(param.url);
        } else {
            this.dynamicPopupService.openPopup({
                data: {
                    title: 'Notice',
                    contents: this.partInfo.disableButtonMessage
                },
                component: MessagePopupContainerComponent
            }, {
                resolver: this.componentFactoryResolver,
                injector: this.injector
            });
        }
    }
}
