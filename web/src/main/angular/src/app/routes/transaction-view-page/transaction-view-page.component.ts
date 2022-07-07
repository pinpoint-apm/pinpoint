import { Component, OnInit, OnDestroy, ComponentFactoryResolver, Injector, ViewChild, AfterViewInit } from '@angular/core';
import { Subject, EMPTY } from 'rxjs';
import { take, takeUntil, switchMap, catchError, filter } from 'rxjs/operators';
import { SplitComponent } from 'angular-split';

import {
    StoreHelperService,
    UrlRouteManagerService,
    NewUrlStateNotificationService,
    TransactionDetailDataService,
    DynamicPopupService,
    GutterEventService,
    WebAppSettingDataService
} from 'app/shared/services';
import { Actions } from 'app/shared/store/reducers';
import { UrlPath, UrlQuery } from 'app/shared/models';
import { ServerErrorPopupContainerComponent } from 'app/core/components/server-error-popup/server-error-popup-container.component';
import { IOutputData } from 'angular-split/lib/interface';
import { MessagePopupContainerComponent } from 'app/core/components/message-popup/message-popup-container.component';

@Component({
    selector: 'pp-transaction-view-page',
    templateUrl: './transaction-view-page.component.html',
    styleUrls: ['./transaction-view-page.component.css'],
})
export class TransactionViewPageComponent implements OnInit, OnDestroy, AfterViewInit {
    @ViewChild('splitElem', {static: false}) splitElem: SplitComponent;
    private unsubscribe = new Subject<void>();
    private errorMessage: string;

    sideNavigationUI: boolean;

    splitSize: number[];

    constructor(
        private storeHelperService: StoreHelperService,
        private newUrlStateNotificationService: NewUrlStateNotificationService,
        private urlRouteManagerService: UrlRouteManagerService,
        private transactionDetailDataService: TransactionDetailDataService,
        private gutterEventService: GutterEventService,
        private dynamicPopupService: DynamicPopupService,
        private componentFactoryResolver: ComponentFactoryResolver,
        private injector: Injector,
        private webAppSettingDataService: WebAppSettingDataService,
    ) { }

    ngOnInit() {
        this.sideNavigationUI = this.webAppSettingDataService.getExperimentalOption('sideNavigationUI');

        this.initSplitRatio();
        this.initTransactionViewInfo();
    }

    ngAfterViewInit() {
        this.splitElem.dragProgress$.subscribe(({sizes}: IOutputData) => {
            this.gutterEventService.resizedGutter(sizes as number[]);
        });
    }

    ngOnDestroy() {
        this.unsubscribe.next();
        this.unsubscribe.complete();
    }

    private initSplitRatio(): void {
        this.gutterEventService.onGutterResized$.pipe(
            take(1)
        ).subscribe((splitSize: number[]) => this.splitSize = splitSize);
    }

    private initTransactionViewInfo(): void {
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

    onGutterResized({sizes}: {sizes: number[]}): void {
        this.gutterEventService.resizedGutter(sizes);
    }
}
