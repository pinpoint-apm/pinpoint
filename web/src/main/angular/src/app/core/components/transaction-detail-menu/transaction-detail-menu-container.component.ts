import { Component, OnInit, OnDestroy, ComponentFactoryResolver, Injector, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { Subject } from 'rxjs';
import { filter, takeUntil } from 'rxjs/operators';

import {
    StoreHelperService,
    UrlRouteManagerService,
    TransactionDetailDataService,
    ITransactionDetailPartInfo,
    AnalyticsService,
    TRACKED_EVENT_LIST,
    DynamicPopupService,
} from 'app/shared/services';
import { UrlPath } from 'app/shared/models';
import { MessagePopupContainerComponent } from 'app/core/components/message-popup/message-popup-container.component';
import { Actions } from 'app/shared/store';
import { parseURL } from 'app/core/utils/url-utils';

@Component({
    selector: 'pp-transaction-detail-menu-container',
    templateUrl: './transaction-detail-menu-container.component.html',
    styleUrls: ['./transaction-detail-menu-container.component.css'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class TransactionDetailMenuContainerComponent implements OnInit, OnDestroy {
    private unsubscribe = new Subject<void>();
    private transactionInfo: ITransactionMetaData;

    activeTabKey: string;
    partInfo: ITransactionDetailPartInfo;

    constructor(
        private storeHelperService: StoreHelperService,
        private urlRouteManagerService: UrlRouteManagerService,
        private transactionDetailDataService: TransactionDetailDataService,
        private analyticsService: AnalyticsService,
        private dynamicPopupService: DynamicPopupService,
        private componentFactoryResolver: ComponentFactoryResolver,
        private injector: Injector,
        private cd: ChangeDetectorRef,
    ) {}

    ngOnInit() {
        this.storeHelperService.getTransactionViewType(this.unsubscribe).subscribe((viewType: string) => {
            this.activeTabKey = viewType;
            this.cd.detectChanges();
        });

        this.transactionDetailDataService.partInfo$.pipe(
            takeUntil(this.unsubscribe)
        ).subscribe((partInfo: ITransactionDetailPartInfo) => {
            this.partInfo = partInfo;
            this.cd.detectChanges();
        });

        this.connectStore();
    }

    ngOnDestroy() {
        this.unsubscribe.next();
        this.unsubscribe.complete();
    }

    private connectStore(): void {
        this.storeHelperService.getTransactionData(this.unsubscribe).pipe(
            filter((data: ITransactionMetaData) => !!data),
            filter(({application, agentId, traceId}: ITransactionMetaData) => !!application && !!agentId && !!traceId)
        ).subscribe((transactionInfo: ITransactionMetaData) => {
            this.transactionInfo = transactionInfo;
            this.cd.detectChanges();
        });
    }

    onSelectViewType(viewType: string): void {
        this.analyticsService.trackEvent((TRACKED_EVENT_LIST as any)[`CLICK_${viewType}`]);
        this.storeHelperService.dispatch(new Actions.ChangeTransactionViewType(viewType));
    }

    onOpenDetailView(): void {
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.OPEN_TRANSACTION_VIEW);
        this.urlRouteManagerService.openPage({
            path: [
                UrlPath.TRANSACTION_VIEW,
                this.transactionInfo.agentId,
                this.transactionInfo.traceId,
                this.transactionInfo.collectorAcceptTime + '',
                this.transactionInfo.spanId,
            ]
        });
    }

    onOpenExtraView(param: any): void {
        if (param.open) {
            this.urlRouteManagerService.openPage(parseURL(param.url));
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
