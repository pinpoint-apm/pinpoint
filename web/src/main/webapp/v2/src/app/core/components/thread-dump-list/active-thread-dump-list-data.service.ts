import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse, HttpParams } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError, retry, tap } from 'rxjs/operators';

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
export class ActiveThreadDumpListDataService {
    requestURL = 'agent/activeThreadLightDump.pinpoint';
    constructor(private http: HttpClient) {}
    getData(applicationName: string, agentId: string): Observable<any> {
        return this.http.get<any>(this.requestURL, this.makeRequestOptionsArgs(applicationName, agentId)).pipe(
            retry(3),
            tap((data: any) => {
                if (data['exception']) {
                    throw data['exception']['message'];
                }
            }),
            catchError(this.handleError)
        );
    }
    private handleError(error: HttpErrorResponse | string) {
        return throwError(error['message'] || error);
    }
    private makeRequestOptionsArgs(applicationName: string, agentId: string): object {
        return {
            params: new HttpParams().set('applicationName', applicationName).set('agentId', agentId)
        };
    }
}
