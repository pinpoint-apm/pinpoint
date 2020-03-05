import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { retry } from 'rxjs/operators';

export interface IActiveThreadDump {
    threadId: string;
    threadName: string;
    threadState: string;
    startTime: number;
    execTime: number;
    localTraceId: number;
    sampled: boolean;
    transactionId: string;
    entryPoint: string;
    detailMessage: string;
}
export interface IActiveThreadDumpResponse {
    code: number;
    message: {
        subType: string;
        threadDumpData: IActiveThreadDump[];
        type: string;
        version: string;
    };
}

@Injectable()
export class ActiveThreadDumpDetailInfoDataService {
    private requestURL = 'agent/activeThreadDump.pinpoint';
    constructor(private http: HttpClient) {}
    getData(applicationName: string, agentId: string, threadName: string, localTraceId: number): Observable<any | AjaxException> {
        return this.http.get<any>(this.requestURL, this.makeRequestOptionsArgs(applicationName, agentId, threadName, localTraceId)).pipe(
            retry(3)
        );
    }
    private makeRequestOptionsArgs(applicationName: string, agentId: string, threadName: string, localTraceId: number): object {
        return {
            params: new HttpParams()
                        .set('applicationName', applicationName)
                        .set('agentId', agentId)
                        .set('threadName', threadName)
                        .set('localTraceId', '' + localTraceId)
        };
    }
}
