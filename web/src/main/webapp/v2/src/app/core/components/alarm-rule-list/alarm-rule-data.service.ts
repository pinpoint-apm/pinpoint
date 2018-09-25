import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError, tap, shareReplay } from 'rxjs/operators';

export interface IAlarmRule {
    applicationId: string;
    checkerName: string;
    emailSend: boolean;
    notes: string;
    ruleId: string;
    serviceType: string;
    smsSend: boolean;
    threshold: number;
    userGroupId: string;
}
export interface IAlarmRuleCreated {
    number: string;
}
export interface IAlarmRuleResponse {
    result: string;
}

@Injectable()
export class AlarmRuleDataService {
    private alarmRuleURL = 'application/alarmRule.pinpoint';
    private checkerListURL = 'application/alarmRule/checker.pinpoint';
    private cache$: Observable<any>;

    constructor(private http: HttpClient) {}
    getCheckerList(): Observable<string[]> {
        if (!this.cache$) {
            const httpRequest$ = this.http.get<string[]>(this.checkerListURL);
            this.cache$ = httpRequest$.pipe(
                tap((data: any) => {
                    if (data.errorCode) {
                        throw data.errorMessage;
                    }
                }),
                catchError(this.handleError),
                shareReplay(1)
            );
        }
        return this.cache$;
    }
    retrieve(applicationId: string): Observable<IAlarmRule[]> {
        return this.http.get<IAlarmRule[]>(this.alarmRuleURL, this.makeRequestOptionsArgs(applicationId)).pipe(
            tap((data: any) => {
                if (data.errorCode) {
                    throw data.errorMessage;
                }
            }),
            catchError(this.handleError)
        );
    }
    create(params: IAlarmRule): Observable<IAlarmRuleCreated> {
        return this.http.post<IAlarmRuleCreated>(this.alarmRuleURL, params).pipe(
            tap(this.checkError),
            catchError(this.handleError)
        );
    }
    update(params: IAlarmRule): Observable<IAlarmRuleResponse> {
        return this.http.put<IAlarmRuleResponse>(this.alarmRuleURL, params).pipe(
            tap(this.checkError),
            catchError(this.handleError)
        );
    }
    remove(ruleId: string): Observable<IAlarmRuleResponse> {
        return this.http.request<IAlarmRuleResponse>('delete', this.alarmRuleURL, {
            body: { ruleId }
        }).pipe(
            tap(this.checkError),
            catchError(this.handleError)
        );
    }
    private checkError(data: any) {
        if (data.errorCode) {
            throw data.errorMessage;
        } else if (data.result !== 'SUCCESS') {
            throw data;
        }
    }
    private handleError(error: HttpErrorResponse) {
        return throwError(error.statusText || error);
    }
    private makeRequestOptionsArgs(applicationId: string): object {
        return applicationId ? {
            params: {
                applicationId
            }
        } : {};
    }
}
