import { Component, Renderer2, OnInit, OnChanges, SimpleChanges, Output, Input, EventEmitter, ViewChild, ElementRef } from '@angular/core';

@Component({
    selector: 'pp-transaction-search',
    templateUrl: './transaction-search.component.html',
    styleUrls: ['./transaction-search.component.css']
})
export class TransactionSearchComponent implements OnInit, OnChanges {
    @ViewChild('searchType', { static: true }) searchType: ElementRef;
    @Input() viewType: string;
    @Input() useArgument: boolean;
    @Input() resultMessage: string;
    @Output() outSearch: EventEmitter<{type: string, query: string}> = new EventEmitter();

    inputValue: string;

    constructor(private renderer: Renderer2) { }
    ngOnInit() {}
    ngOnChanges(changes: SimpleChanges) {
        if (changes['viewType'] && changes['viewType'].currentValue) {
            this.onClear();
            // this.renderer.setAttribute(this.searchType.nativeElement.options[0], 'selected', 'selected');
            this.searchType.nativeElement.options[0].selected = true;
        }
    }

    onSearch(type: string): void {
        const query = this.inputValue.trim();

        if (query === '') {
            return;
        }

        this.outSearch.emit({
            type: type,
            query: query
        });
    }

    onClear() {
        this.inputValue = '';
        this.resultMessage = '';
    }
}
