import { Injectable } from '@angular/core';
import { Subject, Observable } from 'rxjs';

@Injectable()
export class ServerMapInteractionService {
    private outSelectedApplicationSource = new Subject<string>();
    private outRefresh = new Subject<null>();
    private outChangeMergeState = new Subject<IServerMapMergeState>();

    public onSelectedApplication$: Observable<string>;
    public onRefresh$: Observable<null>;
    public onChangeMergeState$: Observable<IServerMapMergeState>;

    constructor() {
        this.onSelectedApplication$ = this.outSelectedApplicationSource.asObservable();
        this.onRefresh$ = this.outRefresh.asObservable();
        this.onChangeMergeState$ = this.outChangeMergeState.asObservable();
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
