import { Component, OnInit, OnDestroy, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { Subject, forkJoin } from 'rxjs';
import { takeUntil, map } from 'rxjs/operators';

import {
    TranslateReplaceService,
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
    private i18nText: { [key: string]: string } = {
        HAS_RESULTS: '',
        EMPTY_RESULT: ''
    };

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
        private translateReplaceService: TranslateReplaceService,
        private analyticsService: AnalyticsService,
        private cd: ChangeDetectorRef
    ) {}

    ngOnInit() {
        this.getI18NText();
        this.transactionSearchInteractionService.onSearchResultCount$.pipe(
            takeUntil(this.unsubscribe),
            // map((count: number) => count === 0 ? this.i18nText.EMPTY_RESULT : this.translateReplaceService.replace(this.i18nText.HAS_RESULTS, count))
        // ).subscribe((message: string) => {
        ).subscribe((resultCount: number) => {
            // this.resultMessage = message;

            // TODO: resultIndex가 maxLength를 넘었을 때 다시 1로 처리해주는거.
            this.resultIndex = resultCount === 0 ? 0 : this.resultIndex + 1;
            this.resultCount = resultCount;
            this.cd.detectChanges();
        });

        this.storeHelperService.getTransactionViewType(this.unsubscribe).subscribe((viewType: string) => {
            // TODO: resultCount = null 로 초기화해주면서 해당영역 지워주기?
            this.resultMessage = '';
            this.currentViewType = viewType;
            this.useArgument = !(viewType === 'timeline');
            this.cd.detectChanges();
        });

        this.storeHelperService.getTransactionData(this.unsubscribe).subscribe(() => {
            // TODO: resultCount = null 로 초기화해주면서 해당영역 지워주기?
            this.resultMessage = '';
            this.cd.detectChanges();
        })
    }

    ngOnDestroy() {
        this.unsubscribe.next();
        this.unsubscribe.complete();
    }

    private getI18NText(): void {
        // forkJoin(
        //     this.translateService.get('TRANSACTION.HAS_RESULTS'),
        //     this.translateService.get('COMMON.EMPTY_ON_SEARCH')
        // ).subscribe((i18n: string[]) => {
        //     this.i18nText.HAS_RESULTS = i18n[0];
        //     this.i18nText.EMPTY_RESULT = i18n[1];
        // });
        this.translateService.get('COMMON.EMPTY_ON_SEARCH').subscribe((emptyMessage: string) => {
            this.emptyMessage = emptyMessage;
        })
    }

    onSearch({type, query, resultIndex}: ISearchParam): void {
        // this.resultMessage = '';

        if (type !== 'exception' && query === '') {
            return;
        }

        this.resultIndex = resultIndex === this.resultCount ? 0 : resultIndex;
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.SEARCH_TRANSACTION, `Search Type: ${type}`);
        this.transactionSearchInteractionService.setSearchParmas({
            type,
            query,
            resultIndex: this.resultIndex
        });
    }
}
