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
import { TransactionTimelineV2Component } from './transaction-timeline-v2.component';
import { Actions } from 'app/shared/store/reducers';

@Component({
    selector: 'pp-transaction-timeline-v2-container',
    templateUrl: './transaction-timeline-v2-container.component.html',
    styleUrls: ['./transaction-timeline-v2-container.component.css'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class TransactionTimelineV2ContainerComponent implements OnInit, OnDestroy {
    @ViewChild(TransactionTimelineV2Component, { static: true }) transactionTimelineComponent: TransactionTimelineV2Component;

    private unsubscribe = new Subject<void>();

    applicationName: string;
    startTime: number;
    endTime: number;
    rowData: any;
    asyncRowData: any;
    focusedRows: boolean[];
    databaseCalls: any;
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
    }

    ngOnDestroy() {
        this.unsubscribe.next();
        this.unsubscribe.complete();
    }

    private connectStore(): void {
        this.storeHelperService.getTransactionTimelineData(this.unsubscribe).pipe(
            filter((transactionTimelineInfo: any) => {
                return transactionTimelineInfo && transactionTimelineInfo.transactionId ? true : false;
            })
        ).subscribe((transactionTimelineInfo: ITransactionTimelineData) => {
            this.startTime = transactionTimelineInfo.callStackStart;
            this.endTime = transactionTimelineInfo.callStackEnd;
            this.barRatio = transactionTimelineInfo.barRatio;
            this.rowData = transactionTimelineInfo.callStack;
            this.asyncRowData = transactionTimelineInfo.asyncCallStack;
            this.databaseCalls = transactionTimelineInfo.databaseCalls;
            this.focusedRows = transactionTimelineInfo.focusedRows;
            this.applicationName = transactionTimelineInfo.applicationId;
            this.cd.detectChanges();
        });
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
