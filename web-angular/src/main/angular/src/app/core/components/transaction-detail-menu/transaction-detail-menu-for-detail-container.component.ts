import { Component, OnInit, ComponentFactoryResolver, Injector } from '@angular/core';
import { Subject } from 'rxjs';

import {
    NewUrlStateNotificationService,
    UrlRouteManagerService,
    AnalyticsService,
    TRACKED_EVENT_LIST,
    DynamicPopupService,
    StoreHelperService
} from 'app/shared/services';
import { UrlPath, UrlQuery } from 'app/shared/models';
import { MessagePopupContainerComponent } from 'app/core/components/message-popup/message-popup-container.component';
import { Actions } from 'app/shared/store/reducers';
import { parseURL } from 'app/core/utils/url-utils';

@Component({
    selector: 'pp-transaction-detail-menu-for-detail-container',
    templateUrl: './transaction-detail-menu-for-detail-container.component.html',
    styleUrls: ['./transaction-detail-menu-for-detail-container.component.css']
})
export class TransactionDetailMenuForDetailContainerComponent implements OnInit {
    private unsubscribe = new Subject<void>();

    activeTabKey: string;
    transactionDetailInfo: ITransactionDetailData;

    constructor(
        private newUrlStateNotificationService: NewUrlStateNotificationService,
        private storeHelperService: StoreHelperService,
        private urlRouteManagerService: UrlRouteManagerService,
        private analyticsService: AnalyticsService,
        private dynamicPopupService: DynamicPopupService,
        private componentFactoryResolver: ComponentFactoryResolver,
        private injector: Injector
    ) {}

    ngOnInit() {
        this.storeHelperService.getTransactionViewType(this.unsubscribe).subscribe((viewType: string) => {
            this.activeTabKey = viewType;
        });

        this.storeHelperService.getTransactionDetailData(this.unsubscribe).subscribe((transactionDetailInfo: ITransactionDetailData) => {
            this.transactionDetailInfo = transactionDetailInfo;
        });
    }

    onSelectViewType(viewType: string): void {
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.SWITCH_TRANSACTION_VIEW_TYPE_THROUGH_TAB, `${viewType}`);
        this.storeHelperService.dispatch(new Actions.ChangeTransactionViewType(viewType));
    }

    onOpenDetailView(): void {
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.OPEN_TRANSACTION_VIEW_PAGE_THROUGH_TAB);
        const {agentId, spanId, traceId, collectorAcceptTime} = JSON.parse(this.newUrlStateNotificationService.getQueryValue(UrlQuery.TRANSACTION_INFO));

        this.urlRouteManagerService.openPage({
            path: [
                UrlPath.TRANSACTION_VIEW
            ],
            queryParams: {
                [UrlQuery.TRANSACTION_INFO]: {agentId, spanId, traceId, collectorAcceptTime}
            }
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
                    contents: this.transactionDetailInfo.disableButtonMessage,
                    type: 'html'
                },
                component: MessagePopupContainerComponent
            }, {
                resolver: this.componentFactoryResolver,
                injector: this.injector
            });
        }
    }
}
