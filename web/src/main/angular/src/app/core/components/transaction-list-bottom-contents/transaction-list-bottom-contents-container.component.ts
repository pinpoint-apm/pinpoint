import { Component, OnInit, OnDestroy, ComponentFactoryResolver, Injector, ViewChild, Renderer2, ElementRef, ChangeDetectorRef } from '@angular/core';
import { EMPTY, Observable, Subject } from 'rxjs';
import { filter, map, switchMap, takeUntil, tap, catchError } from 'rxjs/operators';

import {
    StoreHelperService,
    UrlRouteManagerService,
    TransactionDetailDataService,
    AnalyticsService,
    TRACKED_EVENT_LIST,
    DynamicPopupService,
    NewUrlStateNotificationService,
} from 'app/shared/services';
import { Actions } from 'app/shared/store/reducers';
import { UrlPath, UrlPathId, UrlQuery } from 'app/shared/models';
import { HELP_VIEWER_LIST, HelpViewerPopupContainerComponent } from 'app/core/components/help-viewer-popup/help-viewer-popup-container.component';
import { ServerErrorPopupContainerComponent } from 'app/core/components/server-error-popup/server-error-popup-container.component';
import { CallTreeContainerComponent } from 'app/core/components/call-tree/call-tree-container.component';
import { TranslateService } from '@ngx-translate/core';
import { TransactionMetaDataService } from 'app/core/components/transaction-table-grid/transaction-meta-data.service';

@Component({
    selector: 'pp-transaction-list-bottom-contents-container',
    templateUrl: './transaction-list-bottom-contents-container.component.html',
    styleUrls: ['./transaction-list-bottom-contents-container.component.css']
})
export class TransactionListBottomContentsContainerComponent implements OnInit, OnDestroy {
    @ViewChild(CallTreeContainerComponent, {read: ElementRef, static: false}) callTreeComponent: ElementRef;
    private unsubscribe = new Subject<void>();

    activeView: string;
    transactionInfo: ITransactionMetaData;
    useDisable = true;
    showLoading = true;
    showSearch = true;
    message$: Observable<string>;
    isEmpty = true;
    isTransactionSelected = false;

    constructor(
        private storeHelperService: StoreHelperService,
        private urlRouteManagerService: UrlRouteManagerService,
        private transactionDetailDataService: TransactionDetailDataService,
        private newUrlStateNotificationService: NewUrlStateNotificationService,
        private translateService: TranslateService,
        private transactionMetaDataService: TransactionMetaDataService,
        private analyticsService: AnalyticsService,
        private dynamicPopupService: DynamicPopupService,
        private componentFactoryResolver: ComponentFactoryResolver,
        private injector: Injector,
        private renderer: Renderer2,
        private cd: ChangeDetectorRef
    ) {}

    ngOnInit() {
        this.newUrlStateNotificationService.onUrlStateChange$.pipe(
            takeUntil(this.unsubscribe),
            filter((urlService: NewUrlStateNotificationService) => urlService.hasValue(UrlQuery.TRANSACTION_INFO))
        ).subscribe((_: NewUrlStateNotificationService) => {
            this.isTransactionSelected = true;
            this.cd.detectChanges();
        });

        this.message$ = this.transactionMetaDataService.onTransactionDataLoad$.pipe(
            map(({length}: ITransactionMetaData[]) => length === 0),
            filter((isEmpty: boolean) => !isEmpty),
            switchMap(() => this.translateService.get('TRANSACTION_LIST.SELECT_TRANSACTION'))
        );
        this.connectStore();
    }

    ngOnDestroy() {
        this.unsubscribe.next();
        this.unsubscribe.complete();
    }

    private connectStore(): void {
        this.storeHelperService.getTransactionViewType(this.unsubscribe).pipe(
            filter(() => this.isTransactionSelected),
            tap((viewType: string) => {
                this.renderer.setStyle(this.callTreeComponent.nativeElement, 'display', viewType === 'callTree' ? 'block' : 'none');
            })
        ).subscribe((viewType: string) => {
            this.activeView = viewType;
            this.showSearch = this.activeView === 'callTree' || this.activeView === 'timeline';
        });

        this.storeHelperService.getTransactionData(this.unsubscribe).pipe(
            filter((data: ITransactionMetaData) => !!data),
            filter(({agentId, spanId, traceId, collectorAcceptTime}: ITransactionMetaData) => !!agentId && !!spanId && !!traceId && !!collectorAcceptTime),
            tap(() => {
                this.setDisplayGuide(true);
                // this.renderer.setStyle(this.callTreeComponent.nativeElement, 'display', 'none');
            }),
            tap((transactionInfo: ITransactionMetaData) => this.transactionInfo = transactionInfo),
            switchMap(({agentId, spanId, traceId, collectorAcceptTime}: ITransactionMetaData) => {
                return this.transactionDetailDataService.getData(agentId, spanId, traceId, collectorAcceptTime).pipe(
                    catchError((error: IServerError) => {
                        this.dynamicPopupService.openPopup({
                            data: {
                                title: 'Error',
                                contents: error
                            },
                            component: ServerErrorPopupContainerComponent,
                            onCloseCallback: () => {
                                this.urlRouteManagerService.moveOnPage({
                                    url: [
                                        UrlPath.TRANSACTION_LIST,
                                        this.newUrlStateNotificationService.getPathValue(UrlPathId.APPLICATION).getUrlStr(),
                                        this.newUrlStateNotificationService.getPathValue(UrlPathId.PERIOD).getValueWithTime(),
                                        this.newUrlStateNotificationService.getPathValue(UrlPathId.END_TIME).getEndTime()
                                    ],
                                    queryParams: {
                                        // [UrlQuery.DRAG_INFO]: null,
                                        [UrlQuery.TRANSACTION_INFO]: null
                                    }
                                });
                            }
                        }, {
                            resolver: this.componentFactoryResolver,
                            injector: this.injector
                        });
                        this.setDisplayGuide(false);
                        this.cd.detectChanges();
                        return EMPTY;
                    }),
                );
            }),
            tap(() => {
                this.storeHelperService.dispatch(new Actions.ChangeTransactionViewType('callTree'));
                this.setDisplayGuide(false);
                this.renderer.setStyle(this.callTreeComponent.nativeElement, 'display', 'block');
                this.cd.detectChanges();
            })
        ).subscribe((transactionDetailInfo: ITransactionDetailData) => {
            this.storeHelperService.dispatch(new Actions.UpdateTransactionDetailData(transactionDetailInfo));
        });
    }

    private setDisplayGuide(state: boolean): void {
        this.showLoading = state;
        this.useDisable = state;
    }

    onOpenTransactionDetailPage(): void {
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.OPEN_TRANSACTION_DETAIL_PAGE_WITH_ICON);
        this.urlRouteManagerService.openPage({
            path: [
                UrlPath.TRANSACTION_DETAIL,
            ],
            queryParams: {
                [UrlQuery.TRANSACTION_INFO]: {
                    agentId: this.transactionInfo.agentId,
                    spanId: this.transactionInfo.spanId,
                    traceId: this.transactionInfo.traceId,
                    collectorAcceptTime: this.transactionInfo.collectorAcceptTime
                }
            }
        });
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
