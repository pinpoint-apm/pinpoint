import { Injectable } from '@angular/core';
import { HttpParams } from '@angular/common/http';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface IWebhook {
    url: string;
    alias: string;
    webhookId: string;
    serviceName: string;
    applicationId: string;
}

export interface IWebhookCreate extends Omit<IWebhook, 'webhookId'> { }

export interface IWebhookRule {
    url: string;
    alias?: string;
}

export interface IWebhookAlarm extends Pick<IWebhook, 'webhookId'> {
    ruleId: string;
}

@Injectable()
export class WebhookDataService {
    private apiUrl = 'application/webhook.pinpoint';
    private alarmApiUrl = 'application/webhookSendInfo.pinpoint';

    constructor(
        private http: HttpClient
    ) {}

    getWebhookListByAppId(appName: string): Observable<IWebhook[]> {
        return this.http.get<IWebhook[]>(this.apiUrl, {
            params: new HttpParams().set('applicationId', appName)
        });
    }

    getWebhookListByAlarmId(ruleId: string): Observable<IWebhook[]> {
        return this.http.get<IWebhook[]>(this.apiUrl, {
            params: new HttpParams().set('ruleId', ruleId)
        });
    }

    addWebhook(webhook: IWebhookCreate): Observable<string> {
        return this.http.post<string>(this.apiUrl, { ...webhook });
    }

    editWebhook(webhook: IWebhook): Observable<string> {
        return this.http.put<string>(this.apiUrl, { ...webhook });
    }

    removeWebhook(webhook: IWebhook): Observable<any> {
        return this.http.request<any>('DELETE', this.apiUrl, { body: webhook });
    }

    addWebhookAlarm(webhookIds: string[]) {
        return this.http.post<string>(this.apiUrl, webhookIds);
    }
}
