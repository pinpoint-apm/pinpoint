import { Component, OnInit, OnDestroy, ComponentFactoryResolver, Injector } from '@angular/core';
import { Subject } from 'rxjs';
import { filter } from 'rxjs/operators';

import {
    StoreHelperService,
    UrlRouteManagerService,
    TransactionViewTypeService, VIEW_TYPE,
    TransactionDetailDataService,
    AnalyticsService, TRACKED_EVENT_LIST, DynamicPopupService
} from 'app/shared/services';
import { Actions } from 'app/shared/store';
import { UrlPath } from 'app/shared/models';
import { HELP_VIEWER_LIST, HelpViewerPopupContainerComponent } from 'app/core/components/help-viewer-popup/help-viewer-popup-container.component';
import { ServerErrorPopupContainerComponent } from 'app/core/components/server-error-popup';

@Component({
    selector: 'pp-transaction-list-bottom-contents-container',
    templateUrl: './transaction-list-bottom-contents-container.component.html',
    styleUrls: ['./transaction-list-bottom-contents-container.component.css']
})
export class TransactionListBottomContentsContainerComponent implements OnInit, OnDestroy {
    private unsubscribe: Subject<void> = new Subject();
    currentViewType: string;
    transactionInfo: ITransactionMetaData;
    useDisable = false;
    showLoading = false;
    constructor(
        private storeHelperService: StoreHelperService,
        private urlRouteManagerService: UrlRouteManagerService,
        private transactionDetailDataService: TransactionDetailDataService,
        private transactionViewTypeService: TransactionViewTypeService,
        private analyticsService: AnalyticsService,
        private dynamicPopupService: DynamicPopupService,
        private componentFactoryResolver: ComponentFactoryResolver,
        private injector: Injector
    ) {}
    ngOnInit() {
        this.transactionViewTypeService.onChangeViewType$.subscribe((viewType: string) => {
            this.currentViewType = viewType;
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
                if ( data && data.agentId && data.spanId && data.traceId && data.collectorAcceptTime ) {
                    return true;
                }
                return false;
            })
        ).subscribe((transactionInfo: ITransactionMetaData) => {
            if (this.transactionInfo) {
                this.setDisplayGuide(true);
            }
            this.transactionInfo = transactionInfo;
            this.transactionDetailDataService.getData(
                transactionInfo.agentId,
                transactionInfo.spanId,
                transactionInfo.traceId,
                transactionInfo.collectorAcceptTime
            ).subscribe((transactionDetailInfo: ITransactionDetailData) => {
                this.storeHelperService.dispatch(new Actions.UpdateTransactionDetailData(transactionDetailInfo));
                this.setDisplayGuide(false);
            }, (error: IServerErrorFormat) => {
                this.dynamicPopupService.openPopup({
                    data: {
                        title: 'Error',
                        contents: error
                    },
                    component: ServerErrorPopupContainerComponent
                }, {
                    resolver: this.componentFactoryResolver,
                    injector: this.injector
                });
            });
        });
    }
    private setDisplayGuide(state: boolean): void {
        this.showLoading = state;
        this.useDisable = state;
    }
    isSameType(type: string): boolean {
        return this.currentViewType === type;
    }
    isCallTreeView(): boolean {
        return this.currentViewType === VIEW_TYPE.CALL_TREE || this.currentViewType === VIEW_TYPE.TIMELINE;
    }
    onOpenTransactionDetailPage(): void {
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.OPEN_TRANSACTION_DETAIL);
        this.urlRouteManagerService.openPage([
            UrlPath.TRANSACTION_DETAIL,
            this.transactionInfo.traceId,
            this.transactionInfo.collectorAcceptTime + '',
            this.transactionInfo.agentId,
            this.transactionInfo.spanId
        ]);
    }
    onShowHelp($event: MouseEvent): void {
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.TOGGLE_HELP_VIEWER, HELP_VIEWER_LIST.CALL_TREE);
        const {left, top, width, height} = ($event.target as HTMLElement).getBoundingClientRect();

        this.dynamicPopupService.openPopup({
            data: HELP_VIEWER_LIST.CALL_TREE,
            coord: {
                coordX: left + width / 2,
                coordY: top + height / 2
            },
            component: HelpViewerPopupContainerComponent
        }, {
            resolver: this.componentFactoryResolver,
            injector: this.injector
        });
    }
}
