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
import { TransactionTimelineComponentV2 } from './transaction-timeline-v2.component';
import { Actions } from 'app/shared/store';

@Component({
    selector: 'pp-transaction-timeline-container-v2',
    templateUrl: './transaction-timeline-container-v2.component.html',
    styleUrls: ['./transaction-timeline-container-v2.component.css'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class TransactionTimelineContainerComponentV2 implements OnInit, OnDestroy {
    @ViewChild(TransactionTimelineComponentV2, { static: true }) transactionTimelineComponent: TransactionTimelineComponentV2;

    private unsubscribe = new Subject<void>();

    keyIndex: any;
    startTime: number;
    endTime: number;
    rowData: any;
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
        this.storeHelperService.getTransactionDetailData(this.unsubscribe).pipe(
            filter((transactionDetailInfo: any) => {
                return transactionDetailInfo && transactionDetailInfo.transactionId ? true : false;
            })
        ).subscribe((transactionDetailInfo: ITransactionDetailData) => {
            this.startTime = transactionDetailInfo.callStackStart;
            this.endTime = transactionDetailInfo.callStackEnd;
            this.keyIndex = transactionDetailInfo.callStackIndex;
            this.barRatio = this.getBarRatio(transactionDetailInfo);
            this.rowData = this.filterCallStack(transactionDetailInfo);
            this.cd.detectChanges();
        });
    }

    private getBarRatio(tInfo: ITransactionDetailData): number {
        return 1000 / (tInfo.callStack[0][tInfo.callStackIndex.end] - tInfo.callStack[0][tInfo.callStackIndex.begin]);
    }

    private filterCallStack(rowData: ITransactionDetailData): any {
        let data=[];
        let async=[];
        let asyncRootInfo = [[],[]];
        let isAsync = false;

        rowData.callStack.filter(call =>
            (call[this.keyIndex.isMethod] && !call[this.keyIndex.excludeFromTimeline] && call[this.keyIndex.service] !== ''))
            .forEach((call) => {
                let depth = call[this.keyIndex.tab];
                while (depth >= data.length) {
                    data.push([]);
                    async.push([]);
                }

                if(isAsync) {
                    let asyncInfoLastIndex = asyncRootInfo[0].length - 1;
                    if (call[this.keyIndex.applicationName] != asyncRootInfo[1][asyncInfoLastIndex]) {
                        isAsync = false;
                    } else if (call[this.keyIndex.tab] <= asyncRootInfo[0][asyncInfoLastIndex]) {
                        asyncRootInfo[0].pop();
                        asyncRootInfo[1].pop();
                        isAsync = false;
                    }
                }

                if (call[this.keyIndex.apiType] === "ASYNC") {
                    isAsync = true;
                    asyncRootInfo[0].push(call[this.keyIndex.tab]);
                    asyncRootInfo[1].push(call[this.keyIndex.applicationName]);
                }

                let count = isAsync? async[depth].length : data[depth].length;
                if (count != 0) {
                    let prevSibling = isAsync? async[depth][count-1] : data[depth][count-1];
                    if (call[this.keyIndex.begin] === prevSibling[this.keyIndex.begin]) {
                        if(isAsync) {
                            async[depth].pop();
                        } else {
                            data[depth].pop();
                        }
                    }
                }

                if (isAsync) {
                    async[call[this.keyIndex.tab]].push(call);
                } else {
                    data[call[this.keyIndex.tab]].push(call);
                }
            }
        )
        console.log(async);
        console.log(data);
        return data;
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
