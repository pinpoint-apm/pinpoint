import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError, tap } from 'rxjs/operators';

@Injectable()
export class AgentListDataService {
    private url = 'getAgentList.pinpoint';

    constructor(private http: HttpClient) {}
    retrieve(): Observable<IAgentList> {
        return this.http.get<IAgentList>(this.url).pipe(
            tap((data: any) => {
                if (data.errorCode) {
                    throw data.errorMessage;
                }
            }),
            catchError(this.handleError)
        );
    }
    private handleError(error: HttpErrorResponse) {
        return throwError(error.statusText || error);
    }
}
