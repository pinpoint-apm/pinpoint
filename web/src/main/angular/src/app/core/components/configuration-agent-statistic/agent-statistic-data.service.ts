import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError, tap } from 'rxjs/operators';

@Injectable()
export class AgentStatisticDataService {
    private url = 'getAgentList.pinpoint';
    private lastRequestTime: number;

    constructor(
        private http: HttpClient
    ) {}

    getData(): Observable<IAgentList> {
        return this.http.get<IAgentList>(this.url).pipe(
            tap((data: any) => {
                if (data.errorCode) {
                    throw data.errorMessage;
                }

                this.lastRequestTime = new Date().getTime();
            }),
            catchError(this.handleError)
        );
    }

    getLastRequestTime(): number {
        return this.lastRequestTime;
    }

    private handleError(error: HttpErrorResponse) {
        return throwError(error.statusText || error);
    }
}
