import { Injectable } from '@angular/core';
import { Subject, Observable } from 'rxjs';

@Injectable()
export class ServerMapInteractionService {
    private outSelectedApplicationSource = new Subject<string>();
    private outRefresh = new Subject<null>();

    public onSelectedApplication$: Observable<string>;
    public onRefresh$: Observable<null>;

    constructor() {
        this.onSelectedApplication$ = this.outSelectedApplicationSource.asObservable();
        this.onRefresh$ = this.outRefresh.asObservable();
    }

    setSelectedApplication(appKey: string): void {
        this.outSelectedApplicationSource.next(appKey);
    }
    setRefresh(): void {
        this.outRefresh.next();
    }
}
