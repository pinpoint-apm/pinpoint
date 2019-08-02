import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { retry, shareReplay } from 'rxjs/operators';

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

    constructor(
        private http: HttpClient
    ) {}

    getCheckerList(): Observable<string[]> {
        if (!this.cache$) {
            const httpRequest$ = this.http.get<string[]>(this.checkerListURL);
            this.cache$ = httpRequest$.pipe(
                shareReplay(1)
            );
        }

        return this.cache$;
    }

    retrieve(applicationId: string): Observable<IAlarmRule[]> {
        return this.http.get<IAlarmRule[]>(this.alarmRuleURL, this.makeRequestOptionsArgs(applicationId)).pipe(
            retry(3)
        );
    }

    create(params: {[key: string]: any}): Observable<IAlarmRuleCreated> {
        return this.http.post<IAlarmRuleCreated>(this.alarmRuleURL, params).pipe(
            retry(3)
        );
    }

    update(params: IAlarmRule): Observable<IAlarmRuleResponse> {
        return this.http.put<IAlarmRuleResponse>(this.alarmRuleURL, params).pipe(
            retry(3)
        );
    }

    remove(applicationId: string, ruleId: string): Observable<IAlarmRuleResponse> {
        return this.http.request<IAlarmRuleResponse>('delete', this.alarmRuleURL, {
            body: { applicationId, ruleId }
        }).pipe(
            retry(3)
        );
    }

    private makeRequestOptionsArgs(applicationId: string): object {
        return applicationId
            ? { params: new HttpParams().set('applicationId', applicationId) }
            : {};
    }
}
