import { Injectable } from '@angular/core';
import { HttpParams } from '@angular/common/http';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable()
export class WebhookListDataService {
    private listUrl = 'application/webhook.pinpoint';
    private removeApplicationUrl = 'admin/removeApplicationName.pinpoint';
    private removeAgentUrl = 'admin/removeAgentId.pinpoint';
    // private removeInactiveUrl = 'admin/removeInactiveAgents.pinpoint';

    constructor(
        private http: HttpClient
    ) {}

    getWebhookList(appName: string): Observable<IAgentList> {
        console.log(appName)
        return this.http.get<IAgentList>(this.listUrl, {
            params: new HttpParams().set('applicationId', appName)
        });
    }

    addWebhook({applicationName, password}: {applicationName: string, password: string}): Observable<string> {
        return this.http.post<string>(this.removeApplicationUrl, {
            params: new HttpParams()
                .set('applicationName', applicationName)
                .set('password', password)
        });
    }

    editWebhook({applicationName, agentId, password}: {applicationName: string, agentId: string, password: string}): Observable<string> {
        return this.http.put<string>(this.removeAgentUrl, {
            params: new HttpParams()
                .set('applicationName', applicationName)
                .set('agentId', agentId)
                .set('password', password)
        });
    }
}
