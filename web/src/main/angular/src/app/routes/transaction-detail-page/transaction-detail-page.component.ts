import { Component, OnInit, OnDestroy, ComponentFactoryResolver, Injector } from '@angular/core';
import { Subject } from 'rxjs';
import { takeUntil, filter, switchMap } from 'rxjs/operators';

import {
    StoreHelperService,
    UrlRouteManagerService,
    NewUrlStateNotificationService,
    DynamicPopupService,
    TransactionDetailDataService
} from 'app/shared/services';
import { Actions } from 'app/shared/store';
import { UrlPathId } from 'app/shared/models';
import { ServerErrorPopupContainerComponent } from 'app/core/components/server-error-popup/server-error-popup-container.component';

@Component({
    selector: 'pp-transaction-detail-page',
    templateUrl: './transaction-detail-page.component.html',
    styleUrls: ['./transaction-detail-page.component.css']
})
export class TransactionDetailPageComponent implements OnInit, OnDestroy {
    private unsubscribe: Subject<void> = new Subject();

    constructor(
        private storeHelperService: StoreHelperService,
        private newUrlStateNotificationService: NewUrlStateNotificationService,
        private urlRouteManagerService: UrlRouteManagerService,
        private transactionDetailDataService: TransactionDetailDataService,
        private dynamicPopupService: DynamicPopupService,
        private componentFactoryResolver: ComponentFactoryResolver,
        private injector: Injector
    ) {}

    ngOnInit() {
        this.newUrlStateNotificationService.onUrlStateChange$.pipe(
            takeUntil(this.unsubscribe),
            filter((urlService: NewUrlStateNotificationService) => {
                return urlService.hasValue(UrlPathId.AGENT_ID, UrlPathId.SPAN_ID, UrlPathId.TRACE_ID, UrlPathId.FOCUS_TIMESTAMP);
            }),
            switchMap((urlService: NewUrlStateNotificationService) => {
                return this.transactionDetailDataService.getData(
                    urlService.getPathValue(UrlPathId.AGENT_ID),
                    urlService.getPathValue(UrlPathId.SPAN_ID),
                    urlService.getPathValue(UrlPathId.TRACE_ID),
                    urlService.getPathValue(UrlPathId.FOCUS_TIMESTAMP)
                );
            })
        ).subscribe((transactionDetailInfo: ITransactionDetailData) => {
            this.storeHelperService.dispatch(new Actions.UpdateTransactionDetailData(transactionDetailInfo));
        }, (error: IServerErrorFormat) => {
            this.dynamicPopupService.openPopup({
                data: {
                    title: 'Error',
                    contents: error
                },
                component: ServerErrorPopupContainerComponent,
                onCloseCallback: () => {
                    this.urlRouteManagerService.reload();
                }
            }, {
                resolver: this.componentFactoryResolver,
                injector: this.injector
            });
        });
    }

    ngOnDestroy() {
        this.unsubscribe.next();
        this.unsubscribe.complete();
    }
}
