import { Component, Input, OnInit, OnDestroy, ViewChild, ComponentFactoryResolver, Injector } from '@angular/core';
import { Observable, Subject } from 'rxjs';
import { takeUntil, filter, map, withLatestFrom } from 'rxjs/operators';

import {
    StoreHelperService,
    AnalyticsService,
    TRACKED_EVENT_LIST,
    DynamicPopupService,
    MessageQueueService,
    MESSAGE_TO
} from 'app/shared/services';
import { TransactionSearchInteractionService, ISearchParam } from 'app/core/components/transaction-search/transaction-search-interaction.service';
import { IGridData } from './call-tree.component';
import { CallTreeComponent } from './call-tree.component';
import { MessagePopupContainerComponent } from 'app/core/components/message-popup/message-popup-container.component';
import { SyntaxHighlightPopupContainerComponent } from 'app/core/components/syntax-highlight-popup/syntax-highlight-popup-container.component';

@Component({
    selector: 'pp-call-tree-container',
    templateUrl: './call-tree-container.component.html',
    styleUrls: ['./call-tree-container.component.css'],
})
export class CallTreeContainerComponent implements OnInit, OnDestroy {
    @ViewChild(CallTreeComponent, {static: false}) private callTreeComponent: CallTreeComponent;
    @Input() canSelectRow = false;
    @Input() rowSelection = 'multiple';

    private unsubscribe = new Subject<void>();

    timezone$: Observable<string>;
    dateFormat$: Observable<string>;
    searchSelfTime: number;
    transactionInfo: ITransactionMetaData;
    callTreeData$: Observable<ITransactionDetailData>;
    selectedRowId$: Observable<string>;

    constructor(
        private storeHelperService: StoreHelperService,
        private transactionSearchInteractionService: TransactionSearchInteractionService,
        private analyticsService: AnalyticsService,
        private dynamicPopupService: DynamicPopupService,
        private componentFactoryResolver: ComponentFactoryResolver,
        private injector: Injector,
        private messageQueueService: MessageQueueService,
    ) {}

    ngOnInit() {
        this.connectStore();
        this.transactionSearchInteractionService.onSearch$.pipe(
            takeUntil(this.unsubscribe),
            withLatestFrom(this.storeHelperService.getTransactionViewType(this.unsubscribe)),
            filter(([_, viewType]: [ISearchParam, string]) => viewType === 'callTree'),
            map(([params]) => params)
        ).subscribe((params: ISearchParam) => {
            this.transactionSearchInteractionService.setSearchResultCount(this.callTreeComponent.getQueryedRowCount(params));
        });

        this.selectedRowId$ = this.messageQueueService.receiveMessage(this.unsubscribe, MESSAGE_TO.TRANSACTION_TIMELINE_SELECT_TRANSACTION);
    }

    ngOnDestroy() {
        this.unsubscribe.next();
        this.unsubscribe.complete();
    }

    private connectStore(): void {
        this.timezone$ = this.storeHelperService.getTimezone();
        this.dateFormat$ = this.storeHelperService.getDateFormat(this.unsubscribe, 3);
        this.callTreeData$ = this.storeHelperService.getTransactionDetailData(this.unsubscribe).pipe(
            filter((transactionDetailInfo: ITransactionDetailData) => {
                return transactionDetailInfo && transactionDetailInfo.transactionId ? true : false;
            })
        );
    }

    onSelectFormatting({type, originalContents, bindValue}: any): void {
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.SELECT_SQL);
        this.dynamicPopupService.openPopup({
            data: { type, originalContents, bindValue },
            component: SyntaxHighlightPopupContainerComponent
        }, {
            resolver: this.componentFactoryResolver,
            injector: this.injector
        });
    }

    onRowSelected({startTime, application, agent}: IGridData): void {
        if (startTime === 0) {
            return;
        }

        this.messageQueueService.sendMessage({
            to: MESSAGE_TO.CALL_TREE_ROW_SELECT,
            param: {
                time: startTime,
                applicationId: application,
                agentId: agent
            }
        });
    }

    onCellDoubleClicked(contents: string): void {
        this.dynamicPopupService.openPopup({
            data: {
                title: 'Contents',
                contents
            },
            component: MessagePopupContainerComponent
        }, {
            resolver: this.componentFactoryResolver,
            injector: this.injector
        });
    }
}
