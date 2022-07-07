import { Component, OnInit, OnDestroy, ComponentFactoryResolver, Injector } from '@angular/core';
import { Subject, EMPTY } from 'rxjs';
import { takeUntil, switchMap, catchError, filter } from 'rxjs/operators';
import { TranslateService } from '@ngx-translate/core';

import {
    StoreHelperService,
    UrlRouteManagerService,
    NewUrlStateNotificationService,
    DynamicPopupService,
    TransactionDetailDataService,
    WebAppSettingDataService
} from 'app/shared/services';
import { Actions } from 'app/shared/store/reducers';
import { UrlPath, UrlQuery } from 'app/shared/models';
import { ServerErrorPopupContainerComponent } from 'app/core/components/server-error-popup/server-error-popup-container.component';
import { MessagePopupContainerComponent } from 'app/core/components/message-popup/message-popup-container.component';

@Component({
    selector: 'pp-transaction-detail-page',
    templateUrl: './transaction-detail-page.component.html',
    styleUrls: ['./transaction-detail-page.component.css']
})
export class TransactionDetailPageComponent implements OnInit, OnDestroy {
    private unsubscribe = new Subject<void>();
    private errorMessage: string;

    sideNavigationUI: boolean;

    constructor(
        private storeHelperService: StoreHelperService,
        private newUrlStateNotificationService: NewUrlStateNotificationService,
        private urlRouteManagerService: UrlRouteManagerService,
        private transactionDetailDataService: TransactionDetailDataService,
        private translateService: TranslateService,
        private dynamicPopupService: DynamicPopupService,
        private componentFactoryResolver: ComponentFactoryResolver,
        private injector: Injector,
        private webAppSettingDataService: WebAppSettingDataService,
    ) {}

    ngOnInit() {
        this.sideNavigationUI = this.webAppSettingDataService.getExperimentalOption('sideNavigationUI');
        
        this.translateService.get('TRANSACTION_LIST.TRANSACTION_RETRIEVE_ERROR').subscribe((text: string) => {
            this.errorMessage = text;
        });

        this.newUrlStateNotificationService.onUrlStateChange$.pipe(
            takeUntil(this.unsubscribe),
            filter((urlService: NewUrlStateNotificationService) => {
                if (!urlService.hasValue(UrlQuery.TRANSACTION_INFO)) {
                    this.dynamicPopupService.openPopup({
                        data: {
                            title: 'Notice',
                            contents: this.errorMessage,
                            type: 'html'
                        },
                        component: MessagePopupContainerComponent,
                        onCloseCallback: () => {
                            this.urlRouteManagerService.moveOnPage({
                                url: [
                                    UrlPath.MAIN,
                                ]
                            });
                        }
                    }, {
                        resolver: this.componentFactoryResolver,
                        injector: this.injector
                    });

                    return false;
                }

                return true;
            }),
            switchMap((urlService: NewUrlStateNotificationService) => {
                const {agentId, spanId, traceId, collectorAcceptTime} = JSON.parse(urlService.getQueryValue(UrlQuery.TRANSACTION_INFO));

                return this.transactionDetailDataService.getData(agentId, spanId, traceId, collectorAcceptTime).pipe(
                    catchError((error: IServerError) => {
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

                        return EMPTY;
                    })
                );
            })
        ).subscribe((transactionDetailInfo: ITransactionDetailData) => {
            this.storeHelperService.dispatch(new Actions.UpdateTransactionDetailData(transactionDetailInfo));
        });
    }

    ngOnDestroy() {
        this.unsubscribe.next();
        this.unsubscribe.complete();
    }
}
