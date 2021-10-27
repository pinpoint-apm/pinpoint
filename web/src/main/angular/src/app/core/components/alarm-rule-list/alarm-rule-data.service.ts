import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { shareReplay } from 'rxjs/operators';

export interface IAlarmRule {
    applicationId: string;
    checkerName: string;
    emailSend: boolean;
    notes: string;
    ruleId: string;
    serviceType: string;
    smsSend: boolean;
    webhookSend: boolean;
    threshold: number;
    userGroupId: string;
}
export interface IAlarmRuleCreated {
    number: string;
}
export interface IAlarmRuleResponse {
    result: string;
}

export interface IAlarmWithWebhook {
    rule: IAlarmRule;
    webhookIds: string[];
}

export interface IAlarmRuleDelete extends Pick<IAlarmRule, 'applicationId' | 'ruleId' | 'emailSend' | 'smsSend' | 'webhookSend'> {}

@Injectable()
export class AlarmRuleDataService {
    private alarmRuleURL = 'application/alarmRule.pinpoint';
    private alarmRuleWithWebhookURL = 'application/alarmRule/includeWebhooks.pinpoint';
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
        return this.http.get<IAlarmRule[]>(this.alarmRuleURL, this.makeRequestOptionsArgs(applicationId));
    }

    create(params: {[key: string]: any}): Observable<IAlarmRuleCreated> {
        return this.http.post<IAlarmRuleCreated>(this.alarmRuleURL, params);
    }

    createWithWebhook(params: IAlarmWithWebhook): Observable<IAlarmRuleCreated> {
        return this.http.post<IAlarmRuleCreated>(this.alarmRuleWithWebhookURL, params);
    }

    update(params: IAlarmRule): Observable<IAlarmRuleResponse> {
        return this.http.put<IAlarmRuleResponse>(this.alarmRuleURL, params);
    }

    updateWithWebhook(params: IAlarmWithWebhook): Observable<IAlarmRuleResponse> {
        return this.http.put<IAlarmRuleResponse>(this.alarmRuleWithWebhookURL, params);
    }

    remove(params: IAlarmRuleDelete): Observable<IAlarmRuleResponse> {
        return this.http.request<IAlarmRuleResponse>('delete', this.alarmRuleURL, {
            body: params
        });
    }

    private makeRequestOptionsArgs(applicationId: string): object {
        return applicationId
            ? { params: new HttpParams().set('applicationId', applicationId) }
            : {};
    }
}
