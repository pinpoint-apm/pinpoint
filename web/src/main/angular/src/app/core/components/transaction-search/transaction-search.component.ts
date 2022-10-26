import { Component, OnInit, OnChanges, SimpleChanges, Output, Input, EventEmitter, ViewChild, ElementRef } from '@angular/core';
import { AnalyticsService, TRACKED_EVENT_LIST } from 'app/shared/services';

const enum SearchAction {
    NEW = 'new',
    // SAME,
    PREV = 'prev',
    NEXT = 'next',
}


@Component({
    selector: 'pp-transaction-search',
    templateUrl: './transaction-search.component.html',
    styleUrls: ['./transaction-search.component.css']
})
export class TransactionSearchComponent implements OnInit, OnChanges {
    @ViewChild('searchSelect', {static: true}) searchSelect: ElementRef;
    @Input() viewType: string;
    @Input() useArgument: boolean;
    // @Input() resultIndex: number;
    @Input() resultCount: number;
    @Input() emptyMessage: string;
    @Output() outSearch = new EventEmitter<{type: string, query: string, resultIndex: number}>();

    inputValue: string;
    resultMessage: string;
    isEmpty: boolean = false;
    resultIndex = 0;

    private prevQuery: string;

    constructor(
        private analyticsService: AnalyticsService,
    ) {}
    ngOnInit() {}
    ngOnChanges(changes: SimpleChanges) {
        if (changes['viewType'] && changes['viewType'].currentValue) {
            this.onClear();
            this.searchSelect.nativeElement.options[0].selected = true;
        }

        if (changes['resultCount']) {
            const resultCount = changes['resultCount'].currentValue;

            this.isEmpty = resultCount === 0;
            this.resultIndex = 0;
            this.prevQuery = null;
        }
    }

    onChangeType(): void {
        this.onClear();
        if (this.searchSelect.nativeElement.value === 'exception') {
            this.inputValue = 'Exception';
        }
    }

    onSearch(searchAction?: SearchAction): void {
        const type = this.searchSelect.nativeElement.value;
        const query = this.inputValue.trim();

        if (type !== 'exception' && query === '') {
            return;
        }

        const action = searchAction ? searchAction
            : this.prevQuery === query ? SearchAction.NEXT
            : SearchAction.NEW;

        const resultIndex = this.resultIndex = this.getResultIndex(action);

        this.outSearch.emit({
            type,
            query,
            resultIndex
        });

        this.prevQuery = query;
    }

    private getResultIndex(searchAction: SearchAction): number {
        switch (searchAction) {
            case SearchAction.NEW:
                return 0;
            case SearchAction.PREV:
                return this.resultIndex === 0 ? this.resultCount - 1 : this.resultIndex - 1;
            case SearchAction.NEXT:
                return this.resultIndex === this.resultCount - 1 ? 0 : this.resultIndex + 1;
            default:
                return 0;
        }
    }

    onClear() {
        this.inputValue = '';
        this.resultCount = null;
        this.resultIndex = 0;
    }

    getResultMessage(): string {
        return this.isEmpty ? this.emptyMessage
            :  this.resultCount === null ? ''
            : `${this.resultIndex + 1}/${this.resultCount}`;
    }

    onClickPrev(): void {
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.CLICK_PREV_BUTTON_ON_TRANSACTION_SEARCH);
        this.onSearch(SearchAction.PREV);
    }

    onClickNext(): void {
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.CLICK_NEXT_BUTTON_ON_TRANSACTION_SEARCH);
        this.onSearch(SearchAction.NEXT);
    }
}
