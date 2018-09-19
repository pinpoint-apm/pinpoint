import { Injectable } from '@angular/core';
import { HttpParams } from '@angular/common/http';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError, of } from 'rxjs';
import { catchError, tap } from 'rxjs/operators';


@Injectable()
export class AgentManagerDataService {
    private listUrl = 'getAgentList.pinpoint';
    private removeUrl = 'admin/removeAgentId.pinpoint';
    private removeInactiveUrl = 'admin/removeInactiveAgents.pinpoint';

    constructor(private http: HttpClient) {}
    getAgentList(appName: string): Observable<IAgentList> {
        return this.http.get<IAgentList>(this.listUrl, {
            params: new HttpParams().set('application', appName)
        }).pipe(
            tap((data: any) => {
                if (data.errorCode) {
                    throw data.errorMessage;
                }
            }),
            catchError(this.handleError)
        );
    }
    removeAgentId(appName: string, agentId: string): Observable<string> {
        return this.http.post<string>(this.removeUrl, {
            params: new HttpParams().set('applicationName', appName).set('agentId', agentId)
        }).pipe(
            tap((data: any) => {
                if (data.errorCode) {
                    throw data.errorMessage;
                }
            }),
            catchError(this.handleError)
        );
    }
    removeInactiveAgents(): Observable<string> {
        return this.http.get<string>(this.removeInactiveUrl).pipe(
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
