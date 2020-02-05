import { Component, OnInit, OnDestroy, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { Subject, forkJoin } from 'rxjs';
import { takeUntil, map } from 'rxjs/operators';

import {
    TranslateReplaceService,
    AnalyticsService,
    TRACKED_EVENT_LIST,
    StoreHelperService
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
            map((count: number) => count === 0 ? this.i18nText.EMPTY_RESULT : this.translateReplaceService.replace(this.i18nText.HAS_RESULTS, count))
        ).subscribe((message: string) => {
            this.resultMessage = message;
            this.cd.detectChanges();
        });

        this.storeHelperService.getTransactionViewType(this.unsubscribe).subscribe((viewType: string) => {
            this.resultMessage = '';
            this.currentViewType = viewType;
            this.useArgument = !(viewType === 'timeline');
            this.cd.detectChanges();
        });
    }

    ngOnDestroy() {
        this.unsubscribe.next();
        this.unsubscribe.complete();
    }

    private getI18NText(): void {
        forkJoin(
            this.translateService.get('TRANSACTION.HAS_RESULTS'),
            this.translateService.get('COMMON.EMPTY_ON_SEARCH')
        ).subscribe((i18n: string[]) => {
            this.i18nText.HAS_RESULTS = i18n[0];
            this.i18nText.EMPTY_RESULT = i18n[1];
        });
    }

    onSearch({type, query}: ISearchParam): void {
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.SEARCH_TRANSACTION, `Search Type: ${type}`);
        this.transactionSearchInteractionService.setSearchParmas({
            type,
            query: query === 'self' ? +query : query
        });
    }
}
