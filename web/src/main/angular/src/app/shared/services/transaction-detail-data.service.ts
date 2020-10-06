import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { shareReplay } from 'rxjs/operators';

@Injectable()
export class TransactionDetailDataService {
    private requestURL = 'transactionInfo.pinpoint';
    private requestTimelineURL = 'transactionTimelineInfo.pinpoint';
    private lastKey: string;
    private lastTimelineKey: string;

    cachedData: {[key: string]: Observable<ITransactionDetailData>} = {};
    cachedTimelineData: {[key: string]: Observable<ITransactionTimelineData>} = {};

    constructor(
        private http: HttpClient
    ) {}

    getData(agentId: string, spanId: string, traceId: string, focusTimestamp: number): Observable<ITransactionDetailData> {
        this.lastKey = agentId + spanId + traceId + focusTimestamp;

        if (this.hasData()) {
            return this.cachedData[this.lastKey];
        } else {
            const httpRequest$ = this.http.get<ITransactionDetailData>(this.requestURL, this.makeRequestOptionsArgs(agentId, spanId, traceId, focusTimestamp));

            this.cachedData[this.lastKey] = httpRequest$.pipe(
                shareReplay(3)
            );

            return this.cachedData[this.lastKey];
        }
    }

    private hasData(): boolean {
        return !!this.cachedData[this.lastKey];
    }

    getTimelineData(agentId: string, spanId: string, traceId: string, focusTimestamp: number): Observable<ITransactionTimelineData> {
        this.lastTimelineKey = agentId + spanId + traceId + focusTimestamp;
        if (this.hasTimelineData()) {
            return this.cachedTimelineData[this.lastTimelineKey];
        } else {
            const httpRequest$ = this.http.get<ITransactionTimelineData>(this.requestTimelineURL, this.makeRequestOptionsArgs(agentId, spanId, traceId, focusTimestamp));
            this.cachedTimelineData[this.lastTimelineKey] = httpRequest$.pipe(shareReplay(3));
        }
        return this.cachedTimelineData[this.lastTimelineKey];
    }

    private hasTimelineData(): boolean {
        return !!this.cachedTimelineData[this.lastTimelineKey];
    }

    private makeRequestOptionsArgs(agentId: string, spanId: string, traceId: string, focusTimestamp: number): object {
        return {
            params: new HttpParams()
                .set('agentId', agentId)
                .set('spanId', spanId)
                .set('traceId', traceId)
                .set('focusTimestamp', focusTimestamp + '')
        };
    }
}
