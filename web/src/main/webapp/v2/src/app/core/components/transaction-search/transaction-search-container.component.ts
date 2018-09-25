import { Component, OnInit, ChangeDetectionStrategy, ChangeDetectorRef, OnDestroy } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { combineLatest, Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

import {
    TranslateReplaceService,
    TransactionViewTypeService, VIEW_TYPE,
    AnalyticsService, TRACKED_EVENT_LIST
} from 'app/shared/services';
import { TransactionSearchInteractionService, ISearchParam } from './transaction-search-interaction.service';

@Component({
    selector: 'pp-transaction-search-container',
    templateUrl: './transaction-search-container.component.html',
    styleUrls: ['./transaction-search-container.component.css'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class TransactionSearchContainerComponent implements OnInit, OnDestroy {
    private unsubscribe: Subject<void> = new Subject();
    private i18nText: { [key: string]: string } = {
        HAS_RESULTS: '',
        EMPTY_RESULT: ''
    };
    currentViewType: string;
    useArgument: boolean;
    resultMessage: string;
    constructor(
        private changeDetectorRef: ChangeDetectorRef,
        private transactionSearchInteractionService: TransactionSearchInteractionService,
        private translateService: TranslateService,
        private translateReplaceService: TranslateReplaceService,
        private transactionViewTypeService: TransactionViewTypeService,
        private analyticsService: AnalyticsService,
    ) {}
    ngOnDestroy() {
        this.unsubscribe.next();
        this.unsubscribe.complete();
    }
    ngOnInit() {
        this.getI18NText();
        this.transactionSearchInteractionService.onSearchResult$.pipe(
            takeUntil(this.unsubscribe)
        ).subscribe((result: any) => {
            const resultMessage = result.result === 0 ? this.i18nText.EMPTY_RESULT : this.i18nText.HAS_RESULTS;
            this.resultMessage = this.translateReplaceService.replace(resultMessage, result.result);
            this.changeDetectorRef.detectChanges();
        });
        this.transactionViewTypeService.onChangeViewType$.pipe(
            takeUntil(this.unsubscribe)
        ).subscribe((viewType: string) => {
            this.currentViewType = viewType;
            this.useArgument = viewType === VIEW_TYPE.TIMELINE ? false : true;
            this.changeDetectorRef.detectChanges();
        });
    }
    private getI18NText(): void {
        combineLatest(
            this.translateService.get('TRANSACTION.HAS_RESULTS'),
            this.translateService.get('TRANSACTION.EMPTY_RESULT')
        ).subscribe((i18n: string[]) => {
            this.i18nText.HAS_RESULTS = i18n[0];
            this.i18nText.EMPTY_RESULT = i18n[1];
        });
    }
    onSearch(params: ISearchParam): void {
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.SEARCH_TRANSACTION, `Search Type: ${params.type}`);
        this.transactionSearchInteractionService.setSearchParmas({
            type: params.type,
            query: params.query === 'self' ? +params.query : params.query
        });
    }
}
