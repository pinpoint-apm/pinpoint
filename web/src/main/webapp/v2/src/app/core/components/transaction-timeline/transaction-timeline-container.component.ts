import { Component, OnInit, OnDestroy, ViewChild, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { Subject } from 'rxjs';
import { takeUntil, filter } from 'rxjs/operators';

import {
    StoreHelperService,
    NewUrlStateNotificationService,
    UrlRouteManagerService,
    TransactionViewTypeService, VIEW_TYPE,
    AnalyticsService, TRACKED_EVENT_LIST
} from 'app/shared/services';
import { UrlPath, UrlPathId } from 'app/shared/models';
import { TransactionSearchInteractionService, ISearchParam } from 'app/core/components/transaction-search/transaction-search-interaction.service';
import { TransactionTimelineComponent } from './transaction-timeline.component';

@Component({
    selector: 'pp-transaction-timeline-container',
    templateUrl: './transaction-timeline-container.component.html',
    styleUrls: ['./transaction-timeline-container.component.css'],
    changeDetection: ChangeDetectionStrategy.OnPush
})

export class TransactionTimelineContainerComponent implements OnInit, OnDestroy {
    @ViewChild(TransactionTimelineComponent) private transactionTimelineComponent: TransactionTimelineComponent;
    private unsubscribe: Subject<void> = new Subject();
    hiddenComponent = true;
    keyIndex: any;
    startTime: number;
    endTime: number;
    filteredData: any;
    barRatio: number;
    constructor(
        private changeDetectorRef: ChangeDetectorRef,
        private storeHelperService: StoreHelperService,
        private newUrlStateNotificationService: NewUrlStateNotificationService,
        private urlRouteManagerService: UrlRouteManagerService,
        private transactionViewTypeService: TransactionViewTypeService,
        private transactionSearchInteractionService: TransactionSearchInteractionService,
        private analyticsService: AnalyticsService
    ) {}
    ngOnInit() {
        this.connectStore();
        this.transactionViewTypeService.onChangeViewType$.pipe(
            takeUntil(this.unsubscribe)
        ).subscribe((viewType: string) => {
            if ( viewType === VIEW_TYPE.TIMELINE ) {
                this.hiddenComponent = false;
            } else {
                this.hiddenComponent = true;
            }
            this.changeDetectorRef.detectChanges();
        });
        this.transactionSearchInteractionService.onSearch$.pipe(
            takeUntil(this.unsubscribe)
        ).subscribe((params: ISearchParam) => {
            if (this.hiddenComponent === true) {
                return;
            }
            this.transactionSearchInteractionService.setSearchResult({
                type: params.type,
                query: params.query,
                result: this.transactionTimelineComponent.searchRow(params)
            });
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
            this.changeDetectorRef.detectChanges();

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
        if (this.newUrlStateNotificationService.getStartPath() === UrlPath.TRANSACTION_DETAIL) {
            this.analyticsService.trackEvent(TRACKED_EVENT_LIST.SELECT_TRANSACTION_IN_TIMELINE);
            this.urlRouteManagerService.moveOnPage({
                url: [
                    UrlPath.TRANSACTION_DETAIL,
                    this.newUrlStateNotificationService.getPathValue(UrlPathId.TRACE_ID),
                    this.newUrlStateNotificationService.getPathValue(UrlPathId.FOCUS_TIMESTAMP),
                    this.newUrlStateNotificationService.getPathValue(UrlPathId.AGENT_ID),
                    this.newUrlStateNotificationService.getPathValue(UrlPathId.SPAN_ID),
                    VIEW_TYPE.CALL_TREE,
                    id
                ]
            });
        } else {
            this.urlRouteManagerService.moveOnPage({
                url: [
                    UrlPath.TRANSACTION_LIST,
                    this.newUrlStateNotificationService.getPathValue(UrlPathId.APPLICATION),
                    this.newUrlStateNotificationService.getPathValue(UrlPathId.PERIOD),
                    this.newUrlStateNotificationService.getPathValue(UrlPathId.END_TIME),
                    this.newUrlStateNotificationService.getPathValue(UrlPathId.TRANSACTION_INFO),
                    VIEW_TYPE.CALL_TREE,
                    id
                ]
            });
        }
    }
}
