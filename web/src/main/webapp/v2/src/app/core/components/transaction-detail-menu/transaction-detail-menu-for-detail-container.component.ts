import { Component, OnInit, ComponentFactoryResolver, Injector } from '@angular/core';

import {
    NewUrlStateNotificationService,
    UrlRouteManagerService,
    TransactionViewTypeService,
    IViewType,
    TransactionDetailDataService,
    ITransactionDetailPartInfo,
    AnalyticsService,
    TRACKED_EVENT_LIST,
    DynamicPopupService
} from 'app/shared/services';
import { UrlPath, UrlPathId } from 'app/shared/models';
import { MessagePopupContainerComponent } from 'app/core/components/message-popup/message-popup-container.component';

@Component({
    selector: 'pp-transaction-detail-menu-for-detail-container',
    templateUrl: './transaction-detail-menu-for-detail-container.component.html',
    styleUrls: ['./transaction-detail-menu-for-detail-container.component.css']
})
export class TransactionDetailMenuForDetailContainerComponent implements OnInit {
    viewTypeList: IViewType[];
    viewType: string;
    partInfo: ITransactionDetailPartInfo;
    constructor(
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
        this.transactionViewTypeService.onChangeViewType$.subscribe((viewType: string) => {
            this.viewType = viewType;
        });
        this.transactionDetailDataService.partInfo$.subscribe((partInfo: ITransactionDetailPartInfo) => {
            this.partInfo = partInfo;
        });
    }
    onSelectViewType(viewType: string): void {
        this.analyticsService.trackEvent((TRACKED_EVENT_LIST as any)[`CLICK_${viewType}`]);
        this.urlRouteManagerService.moveOnPage({
            url: [
                UrlPath.TRANSACTION_DETAIL,
                this.newUrlStateNotificationService.getPathValue(UrlPathId.TRACE_ID),
                this.newUrlStateNotificationService.getPathValue(UrlPathId.FOCUS_TIMESTAMP),
                this.newUrlStateNotificationService.getPathValue(UrlPathId.AGENT_ID),
                this.newUrlStateNotificationService.getPathValue(UrlPathId.SPAN_ID),
                viewType
            ]
        });
    }
    onOpenDetailView(): void {
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.OPEN_TRANSACTION_VIEW);
        this.urlRouteManagerService.openPage([
            UrlPath.TRANSACTION_VIEW,
            this.newUrlStateNotificationService.getPathValue(UrlPathId.AGENT_ID),
            this.newUrlStateNotificationService.getPathValue(UrlPathId.TRACE_ID),
            this.newUrlStateNotificationService.getPathValue(UrlPathId.FOCUS_TIMESTAMP),
            this.newUrlStateNotificationService.getPathValue(UrlPathId.SPAN_ID)
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
