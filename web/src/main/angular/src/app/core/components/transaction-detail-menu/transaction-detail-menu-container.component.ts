import { Component, OnInit, OnDestroy, ComponentFactoryResolver, Injector, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { Subject } from 'rxjs';
import { filter } from 'rxjs/operators';

import {
    StoreHelperService,
    UrlRouteManagerService,
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
    transactionDetailInfo: ITransactionDetailData;

    constructor(
        private storeHelperService: StoreHelperService,
        private urlRouteManagerService: UrlRouteManagerService,
        private analyticsService: AnalyticsService,
        private dynamicPopupService: DynamicPopupService,
        private componentFactoryResolver: ComponentFactoryResolver,
        private injector: Injector,
        private cd: ChangeDetectorRef,
    ) {}

    ngOnInit() {
        this.connectStore();
    }

    ngOnDestroy() {
        this.unsubscribe.next();
        this.unsubscribe.complete();
    }

    private connectStore(): void {
        this.storeHelperService.getTransactionViewType(this.unsubscribe).subscribe((viewType: string) => {
            this.activeTabKey = viewType;
            this.cd.detectChanges();
        });

        this.storeHelperService.getTransactionData(this.unsubscribe).pipe(
            filter((data: ITransactionMetaData) => !!data),
            filter(({application, agentId, traceId}: ITransactionMetaData) => !!application && !!agentId && !!traceId)
        ).subscribe((transactionInfo: ITransactionMetaData) => {
            this.transactionInfo = transactionInfo;
            this.cd.detectChanges();
        });

        this.storeHelperService.getTransactionDetailData(this.unsubscribe).subscribe((transactionDetailInfo: ITransactionDetailData) => {
            this.transactionDetailInfo = transactionDetailInfo;
            this.cd.detectChanges();
        });
    }

    onSelectViewType(viewType: string): void {
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.SWITCH_TRANSACTION_VIEW_TYPE_THROUGH_TAB, `${viewType}`);
        this.storeHelperService.dispatch(new Actions.ChangeTransactionViewType(viewType));
    }

    onOpenDetailView(): void {
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.OPEN_TRANSACTION_VIEW_PAGE_THROUGH_TAB);
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
            this.analyticsService.trackEvent(TRACKED_EVENT_LIST.OPEN_LOG_PAGE_THROUGH_TAB);
            this.urlRouteManagerService.openPage(parseURL(param.url));
        } else {
            this.dynamicPopupService.openPopup({
                data: {
                    title: 'Notice',
                    contents: this.transactionDetailInfo.disableButtonMessage
                },
                component: MessagePopupContainerComponent
            }, {
                resolver: this.componentFactoryResolver,
                injector: this.injector
            });
        }
    }
}
