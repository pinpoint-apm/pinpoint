import { Component, OnInit, OnDestroy, ViewChild, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { Subject } from 'rxjs';
import { takeUntil, filter } from 'rxjs/operators';

import {
    StoreHelperService,
    AnalyticsService,
    TRACKED_EVENT_LIST,
    MessageQueueService,
    MESSAGE_TO
} from 'app/shared/services';
import { TransactionSearchInteractionService, ISearchParam } from 'app/core/components/transaction-search/transaction-search-interaction.service';
import { TransactionTimelineComponent } from './transaction-timeline.component';
import { Actions } from 'app/shared/store';

@Component({
    selector: 'pp-transaction-timeline-container',
    templateUrl: './transaction-timeline-container.component.html',
    styleUrls: ['./transaction-timeline-container.component.css'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class TransactionTimelineContainerComponent implements OnInit, OnDestroy {
    @ViewChild(TransactionTimelineComponent, { static: true }) transactionTimelineComponent: TransactionTimelineComponent;

    private unsubscribe = new Subject<void>();

    keyIndex: any;
    startTime: number;
    endTime: number;
    filteredData: any;
    barRatio: number;

    constructor(
        private storeHelperService: StoreHelperService,
        private transactionSearchInteractionService: TransactionSearchInteractionService,
        private messageQueueService: MessageQueueService,
        private analyticsService: AnalyticsService,
        private cd: ChangeDetectorRef
    ) {}

    ngOnInit() {
        this.connectStore();
        this.transactionSearchInteractionService.onSearch$.pipe(
            takeUntil(this.unsubscribe),
        ).subscribe((params: ISearchParam) => {
            this.transactionSearchInteractionService.setSearchResultCount(this.transactionTimelineComponent.searchRow(params));
        });
    }

    ngOnDestroy() {
        this.unsubscribe.next();
        this.unsubscribe.complete();
    }

    private connectStore(): void {
        this.storeHelperService.getTransactionDetailData(this.unsubscribe).pipe(
            filter((transactionDetailInfo: any) => {
                return transactionDetailInfo && transactionDetailInfo.transactionId ? true : false;
            })
        ).subscribe((transactionDetailInfo: ITransactionDetailData) => {
            this.startTime = transactionDetailInfo.callStackStart;
            this.endTime = transactionDetailInfo.callStackEnd;
            this.keyIndex = transactionDetailInfo.callStackIndex;
            this.barRatio = this.getBarRatio(transactionDetailInfo);
            this.filteredData = this.filterCallStack(transactionDetailInfo);
            this.cd.detectChanges();
        });
    }

    private getBarRatio(tInfo: ITransactionDetailData): number {
        return 1000 / (tInfo.callStack[0][tInfo.callStackIndex.end] - tInfo.callStack[0][tInfo.callStackIndex.begin]);
    }

    private filterCallStack(rowData: ITransactionDetailData): any {
        const newCallStacks: any = [];

        rowData.callStack.forEach((call: any) => {
            if (call[this.keyIndex.isMethod] && !call[this.keyIndex.excludeFromTimeline] && call[this.keyIndex.service] !== '') {
                newCallStacks.push(call);
            }
        });

        return newCallStacks;
    }

    onSelectTransaction(id: string): void {
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.SELECT_TRANSACTION_IN_TIMELINE);
        this.messageQueueService.sendMessage({
            to: MESSAGE_TO.TRANSACTION_TIMELINE_SELECT_TRANSACTION,
            param: id
        });
        this.storeHelperService.dispatch(new Actions.ChangeTransactionViewType('callTree'));
    }
}
