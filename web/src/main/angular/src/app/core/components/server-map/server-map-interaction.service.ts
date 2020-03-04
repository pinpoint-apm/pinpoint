import { Injectable } from '@angular/core';
import { Subject, Observable } from 'rxjs';

@Injectable()
export class ServerMapInteractionService {
    private outSearchWordSource = new Subject<string>();
    private outSearchResultSource = new Subject<IApplication[]>();
    private outSelectedApplicationSource = new Subject<string>();
    private outRefresh = new Subject<null>();
    private outChangeMergeState = new Subject<IServerMapMergeState>();

    public onSearchWord$: Observable<string>;
    public onSearchResult$: Observable<IApplication[]>;
    public onSelectedApplication$: Observable<string>;
    public onRefresh$: Observable<null>;
    public onChangeMergeState$: Observable<IServerMapMergeState>;

    constructor() {
        this.onSearchWord$ = this.outSearchWordSource.asObservable();
        this.onSearchResult$ = this.outSearchResultSource.asObservable();
        this.onSelectedApplication$ = this.outSelectedApplicationSource.asObservable();
        this.onRefresh$ = this.outRefresh.asObservable();
        this.onChangeMergeState$ = this.outChangeMergeState.asObservable();
    }

    setSearchWord(word: string): void {
        this.outSearchWordSource.next(word);
    }
    setSearchResult(result: IApplication[]): void {
        this.outSearchResultSource.next(result);
    }
    setSelectedApplication(appKey: string): void {
        this.outSelectedApplicationSource.next(appKey);
    }
    setRefresh(): void {
        this.outRefresh.next();
    }
    setMergeState(mergeState: IServerMapMergeState): void {
        this.outChangeMergeState.next(mergeState);
    }
}
