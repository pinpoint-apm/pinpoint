import { Injectable } from '@angular/core';
import { Subject, Observable } from 'rxjs';

import { WebAppSettingDataService } from 'app/shared/services';

@Injectable()
export class ServerMapRangeHandlerService {
    private reservedNextTo: number;
    private outFetchCompleted = new Subject<{range: number[], delay: number}>();

    onFetchCompleted$: Observable<{range: number[], delay: number}>;

    constructor(
        private webAppSettingDataService: WebAppSettingDataService,
    ) {
        this.onFetchCompleted$ = this.outFetchCompleted.asObservable();
    }

    setReservedNextTo(to: number): void {
        this.reservedNextTo = to;
    }

    onFetchCompleted(fetchCompletedTime: number): void {
        const isDelayed = fetchCompletedTime > this.reservedNextTo;
        const nextTo = isDelayed ? fetchCompletedTime : this.reservedNextTo;
        const from = nextTo - this.webAppSettingDataService.getSystemDefaultPeriod().getMiliSeconds();
        const delay = isDelayed ? 0 : this.reservedNextTo - fetchCompletedTime;

        this.outFetchCompleted.next({
            range: [from, nextTo],
            delay
        });
    }
}
