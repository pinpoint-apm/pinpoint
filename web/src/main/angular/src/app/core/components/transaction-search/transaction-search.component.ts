import { Component, OnInit, OnChanges, SimpleChanges, Output, Input, EventEmitter, ViewChild, ElementRef } from '@angular/core';

@Component({
    selector: 'pp-transaction-search',
    templateUrl: './transaction-search.component.html',
    styleUrls: ['./transaction-search.component.css']
})
export class TransactionSearchComponent implements OnInit, OnChanges {
    @ViewChild('searchType', { static: true }) searchType: ElementRef;
    @Input() viewType: string;
    @Input() useArgument: boolean;
    // @Input() resultMessage: string;
    @Input() resultIndex: number;
    @Input() resultCount: number;
    @Input() emptyMessage: string;
    @Output() outSearch = new EventEmitter<{type: string, query: string, resultIndex: number}>();

    inputValue: string;
    resultMessage: string;
    isEmpty: boolean = false;

    constructor() {}
    ngOnInit() {}
    ngOnChanges(changes: SimpleChanges) {
        if (changes['viewType'] && changes['viewType'].currentValue) {
            this.onClear();
            // this.renderer.setAttribute(this.searchType.nativeElement.options[0], 'selected', 'selected');
            this.searchType.nativeElement.options[0].selected = true;
        }

        if (changes['resultCount']) {
            const resultCount = changes['resultCount'].currentValue;

            this.isEmpty = resultCount === 0;
            // this.resultMessage = this.isEmpty ? this.emptyMessage
            //     : resultCount === null ? ''
            //     : `${resultIndex}/${resultCount}`;
        }
    }

    onChangeType(): void {
        this.resultIndex = 0;
    }

    onSearch(type: string): void {
        // TODO: 여기에서 index를 관리해야될듯? type onchange나 transaction select 바뀔때마다 search index 초기화하고.
        const query = this.inputValue.trim();

        this.outSearch.emit({
            type: type,
            query: query,
            resultIndex: this.resultIndex
        });
    }

    onClear() {
        this.inputValue = '';
        this.resultMessage = '';
    }

    getResultMessage(): string {
        return this.isEmpty ? this.emptyMessage
            :  this.resultCount === null ? ''
            : `${this.resultIndex}/${this.resultCount}`;
    }
}
