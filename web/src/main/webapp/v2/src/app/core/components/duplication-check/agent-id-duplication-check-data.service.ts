import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';

export interface IAgentIdAvailable {
    code: number;
    message: string;
}

@Injectable()
export class AgentIdDuplicationCheckDataService {
    private requestURL = 'isAvailableAgentId.pinpoint';

    constructor(
        private http: HttpClient,
    ) {}

    getResponseWithParams(value: string): Observable<IAgentIdAvailable> {
        return this.http.get<IAgentIdAvailable>(this.requestURL, this.makeParams({agentId: value})).pipe(
            catchError(this.handleError)
        );
    }
    private makeParams(paramObj: object): object {
        return {
            params: { ...paramObj }
        };
    }
    private handleError(error: HttpErrorResponse) {
        return throwError(error.statusText);
    }
}
