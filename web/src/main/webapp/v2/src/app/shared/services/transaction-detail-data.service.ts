import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

import { Observable, Subject } from 'rxjs';
import { tap, shareReplay } from 'rxjs/operators';

export interface ITransactionDetailPartInfo {
    completeState: string;
    logPageUrl: string;
    logButtonName: string;
    logLinkEnable: boolean;
    disableButtonMessage: string;
    loggingTransactionInfo: boolean;
}

@Injectable()
export class TransactionDetailDataService {
    private partInfo: Subject<any> = new Subject();
    lastKey: string;
    cachedData: { [key: string]: Observable<ITransactionDetailData> } = {};
    partInfo$: Observable<any>;
    requestURL = 'transactionInfo.pinpoint';
    constructor(private http: HttpClient) {
        this.partInfo$ = this.partInfo.asObservable();
    }
    getData(agentId: string, spanId: string, traceId: string, focusTimestamp: number): Observable<ITransactionDetailData> {
        this.lastKey = agentId + spanId + traceId + focusTimestamp;
        if ( this.hasData() ) {
            return this.cachedData[this.lastKey];
        } else {
            const httpRequest$ = this.http.get<ITransactionDetailData>(this.requestURL, this.makeRequestOptionsArgs(agentId, spanId, traceId, focusTimestamp));
            this.cachedData[this.lastKey] = httpRequest$.pipe(
                tap((transactionInfo: any) => {
                    this.partInfo.next({
                        logButtonName: transactionInfo.logButtonName,
                        logLinkEnable: transactionInfo.logLinkEnable,
                        logPageUrl: transactionInfo.logPageUrl,
                        loggingTransactionInfo: transactionInfo.loggingTransactionInfo,
                        disableButtonMessage: transactionInfo.disableButtonMessage,
                        completeState: transactionInfo.completeState,
                    });
                }),
                shareReplay(1)
            );
            return this.cachedData[this.lastKey];
        }
    }
    private hasData(): boolean {
        return !!this.cachedData[this.lastKey];
    }
    private makeRequestOptionsArgs(agentId: string, spanId: string, traceId: string, focusTimestamp: number): object {
        return {
            params: {
                agentId: agentId,
                spanId: spanId,
                traceId: traceId,
                focusTimestamp: focusTimestamp
            }
        };
    }
}
