import { Component, OnInit, ComponentFactoryResolver, Injector } from '@angular/core';
import { Subject } from 'rxjs';

import {
    NewUrlStateNotificationService,
    UrlRouteManagerService,
    TransactionDetailDataService,
    ITransactionDetailPartInfo,
    AnalyticsService,
    TRACKED_EVENT_LIST,
    DynamicPopupService,
    StoreHelperService
} from 'app/shared/services';
import { UrlPath, UrlPathId } from 'app/shared/models';
import { MessagePopupContainerComponent } from 'app/core/components/message-popup/message-popup-container.component';
import { Actions } from 'app/shared/store';

@Component({
    selector: 'pp-transaction-detail-menu-for-detail-container',
    templateUrl: './transaction-detail-menu-for-detail-container.component.html',
    styleUrls: ['./transaction-detail-menu-for-detail-container.component.css']
})
export class TransactionDetailMenuForDetailContainerComponent implements OnInit {
    private unsubscribe = new Subject<void>();

    activeTabKey: string;
    partInfo: ITransactionDetailPartInfo;

    constructor(
        private newUrlStateNotificationService: NewUrlStateNotificationService,
        private storeHelperService: StoreHelperService,
        private urlRouteManagerService: UrlRouteManagerService,
        private transactionDetailDataService: TransactionDetailDataService,
        private analyticsService: AnalyticsService,
        private dynamicPopupService: DynamicPopupService,
        private componentFactoryResolver: ComponentFactoryResolver,
        private injector: Injector
    ) {}

    ngOnInit() {
        this.storeHelperService.getTransactionViewType(this.unsubscribe).subscribe((viewType: string) => {
            this.activeTabKey = viewType;
        });

        this.transactionDetailDataService.partInfo$.subscribe((partInfo: ITransactionDetailPartInfo) => {
            this.partInfo = partInfo;
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
                this.newUrlStateNotificationService.getPathValue(UrlPathId.AGENT_ID),
                this.newUrlStateNotificationService.getPathValue(UrlPathId.TRACE_ID),
                this.newUrlStateNotificationService.getPathValue(UrlPathId.FOCUS_TIMESTAMP),
                this.newUrlStateNotificationService.getPathValue(UrlPathId.SPAN_ID)
            ]
        });
    }

    onOpenExtraView(param: any): void {
        if (param.open) {
            this.urlRouteManagerService.openPage({
                path: [param.url]
            });
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
