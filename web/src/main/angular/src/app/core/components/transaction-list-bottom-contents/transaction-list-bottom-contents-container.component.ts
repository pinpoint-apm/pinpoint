import { Component, OnInit, OnDestroy, ComponentFactoryResolver, Injector, ViewChild, Renderer2, ElementRef, ChangeDetectorRef } from '@angular/core';
import { Observable, Subject } from 'rxjs';
import { filter, map, switchMap, tap } from 'rxjs/operators';

import {
    StoreHelperService,
    UrlRouteManagerService,
    TransactionDetailDataService,
    AnalyticsService,
    TRACKED_EVENT_LIST,
    DynamicPopupService,
} from 'app/shared/services';
import { Actions } from 'app/shared/store';
import { UrlPath } from 'app/shared/models';
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
    removeCallTree = false;
    showSearch: boolean;
    message$: Observable<string>;
    isEmpty = true;
    isTransactionSelected = false;

    constructor(
        private storeHelperService: StoreHelperService,
        private urlRouteManagerService: UrlRouteManagerService,
        private transactionDetailDataService: TransactionDetailDataService,
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
            tap(() => this.isTransactionSelected = true),
            filter(({agentId, spanId, traceId, collectorAcceptTime}: ITransactionMetaData) => !!agentId && !!spanId && !!traceId && !!collectorAcceptTime),
            tap(() => {
                this.setDisplayGuide(true);
                // this.renderer.setStyle(this.callTreeComponent.nativeElement, 'display', 'none');
            }),
            tap((transactionInfo: ITransactionMetaData) => this.transactionInfo = transactionInfo)
        ).subscribe(({agentId, spanId, traceId, collectorAcceptTime}: ITransactionMetaData) => {
            this.transactionDetailDataService.getData(agentId, spanId, traceId, collectorAcceptTime).subscribe((transactionDetailInfo: ITransactionDetailData) => {
                this.storeHelperService.dispatch(new Actions.UpdateTransactionDetailData(transactionDetailInfo));
                this.storeHelperService.dispatch(new Actions.ChangeTransactionViewType('callTree'));
                this.setDisplayGuide(false);
                this.renderer.setStyle(this.callTreeComponent.nativeElement, 'display', 'block');
            });
            this.transactionDetailDataService.getTimelineData(agentId, spanId, traceId, collectorAcceptTime).subscribe((transactionTimelineInfo: ITransactionTimelineData) => {
                this.storeHelperService.dispatch(new Actions.UpdateTransactionTimelineData(transactionTimelineInfo));
            });
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
            this.setDisplayGuide(false);
            this.cd.detectChanges();
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
                this.transactionInfo.traceId,
                this.transactionInfo.collectorAcceptTime + '',
                this.transactionInfo.agentId,
                this.transactionInfo.spanId
            ]
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
