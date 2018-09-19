import { Injectable } from '@angular/core';
import { Subject, Observable } from 'rxjs';

export interface ISearchParam {
    type: string;
    query: string | number;
}

@Injectable()
export class TransactionSearchInteractionService {
    private search = new Subject<ISearchParam>();
    private searchResult = new Subject<any>();
    private moveRow = new Subject<string>();

    onSearch$: Observable<ISearchParam>;
    onSearchResult$: Observable<any>;
    onMoveRow$: Observable<string>;

    constructor() {
        this.onSearch$ = this.search.asObservable();
        this.onSearchResult$ = this.searchResult.asObservable();
        this.onMoveRow$ = this.moveRow.asObservable();
    }
    setSearchParmas(params: ISearchParam): void {
        this.search.next(params);
    }
    setSearchResult(result: any): void {
        this.searchResult.next(result);
    }
    setRow(id: string): void {
        this.moveRow.next(id);
    }
}

