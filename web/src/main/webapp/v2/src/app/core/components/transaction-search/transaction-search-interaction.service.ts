import { Injectable } from '@angular/core';
import { Subject, Observable } from 'rxjs';

export interface ISearchParam {
    type: string;
    query: string | number;
}

@Injectable()
export class TransactionSearchInteractionService {
    private search = new Subject<ISearchParam>();
    private searchResultCount = new Subject<number>();
    private moveRow = new Subject<string>();

    onSearch$: Observable<ISearchParam>;
    onSearchResultCount$: Observable<number>;
    onMoveRow$: Observable<string>;

    constructor() {
        this.onSearch$ = this.search.asObservable();
        this.onSearchResultCount$ = this.searchResultCount.asObservable();
        this.onMoveRow$ = this.moveRow.asObservable();
    }

    setSearchParmas(params: ISearchParam): void {
        this.search.next(params);
    }

    setSearchResultCount(count: number): void {
        this.searchResultCount.next(count);
    }

    setRow(id: string): void {
        this.moveRow.next(id);
    }
}

