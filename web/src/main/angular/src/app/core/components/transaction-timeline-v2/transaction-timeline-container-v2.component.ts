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
        return Math.max(1000 / (tInfo.callStackEnd - tInfo.callStackStart), 1);
    }

    private filterCallStack(data: ITransactionDetailData): any {
        let rowData=[];
        let asyncRootInfo = [[],[]];        /* depth, application name*/
        let isAsync = false;
        let prevApplication = "";

        function checkAsyncDepth(call): number {
            let asyncInfoLastIndex = asyncRootInfo[0].length - 1;
            while (call[this.keyIndex.tab] <= asyncRootInfo[0][asyncInfoLastIndex]) {
                asyncRootInfo[0].pop();
                asyncRootInfo[1].pop();
                asyncInfoLastIndex--;
                isAsync = false;
            }
            return asyncInfoLastIndex;
        }

        data.callStack.filter(call =>
            (call[this.keyIndex.isMethod] && !call[this.keyIndex.excludeFromTimeline] && call[this.keyIndex.service] !== ''))
            .forEach((call) => {
                let depth = call[this.keyIndex.tab];
                while (depth >= rowData.length) {
                    rowData.push([]);
                }

                let asyncInfoLastIndex = checkAsyncDepth.call(this, call);

                if (asyncInfoLastIndex >= 0) {
                    if (call[this.keyIndex.applicationName] === asyncRootInfo[1][asyncInfoLastIndex]) {
                        if (call[this.keyIndex.tab] > asyncRootInfo[0][asyncInfoLastIndex]) {
                            // returned to previous async trace
                            isAsync = true;
                        }
                    } else if (call[this.keyIndex.applicationName] != prevApplication){
                        // now at synchronous application trace
                        isAsync = false;
                    }
                }

                if (call[this.keyIndex.apiType] === "ASYNC") {
                    isAsync = true
                    asyncRootInfo[0].push(call[this.keyIndex.tab]);
                    asyncRootInfo[1].push(call[this.keyIndex.applicationName]);
                }

                let rearranged = [];
                if (isAsync) {
                    rearranged.push(null);
                    rearranged.push(call);
                } else {
                    rearranged.push(call);
                    rearranged.push(null);
                }
                rowData[depth].push(rearranged);
                prevApplication = call[this.keyIndex.applicationName];
            }
        )
        return rowData;
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
