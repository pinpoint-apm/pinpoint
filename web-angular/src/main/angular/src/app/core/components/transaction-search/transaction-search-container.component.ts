import { Component, OnInit, OnDestroy, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

import {
    AnalyticsService,
    TRACKED_EVENT_LIST,
    StoreHelperService,
} from 'app/shared/services';
import { TransactionSearchInteractionService, ISearchParam } from './transaction-search-interaction.service';

@Component({
    selector: 'pp-transaction-search-container',
    templateUrl: './transaction-search-container.component.html',
    styleUrls: ['./transaction-search-container.component.css'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class TransactionSearchContainerComponent implements OnInit, OnDestroy {
    private unsubscribe = new Subject<void>();

    currentViewType: string;
    useArgument: boolean;
    resultMessage: string;

    emptyMessage: string;
    resultIndex: number = 0;
    resultCount: number = null;

    constructor(
        private storeHelperService: StoreHelperService,
        private transactionSearchInteractionService: TransactionSearchInteractionService,
        private translateService: TranslateService,
        private analyticsService: AnalyticsService,
        private cd: ChangeDetectorRef
    ) {}

    ngOnInit() {
        this.getI18NText();
        this.transactionSearchInteractionService.onSearchResultCount$.pipe(
            takeUntil(this.unsubscribe),
        ).subscribe((resultCount: number) => {
            this.resultCount = resultCount;
            this.cd.detectChanges();
        });

        this.storeHelperService.getTransactionViewType(this.unsubscribe).subscribe((viewType: string) => {
            this.resultCount = null;
            this.currentViewType = viewType;
            this.useArgument = !(viewType === 'timeline');
            this.cd.detectChanges();
        });

        this.storeHelperService.getTransactionData(this.unsubscribe).subscribe(() => {
            this.resultCount = null;
            this.cd.detectChanges();
        })
    }

    ngOnDestroy() {
        this.unsubscribe.next();
        this.unsubscribe.complete();
    }

    private getI18NText(): void {
        this.translateService.get('COMMON.EMPTY_ON_SEARCH').subscribe((emptyMessage: string) => {
            this.emptyMessage = emptyMessage;
        })
    }

    onSearch({type, query, resultIndex}: ISearchParam): void {
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.SEARCH_TRANSACTION, `Search Type: ${type}`);
        this.transactionSearchInteractionService.setSearchParmas({
            type,
            query,
            resultIndex
        });
    }
}
