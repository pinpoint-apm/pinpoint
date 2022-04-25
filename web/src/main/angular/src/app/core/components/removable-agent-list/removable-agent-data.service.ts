import { Injectable } from '@angular/core';
import { HttpParams } from '@angular/common/http';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable()
export class RemovableAgentDataService {
    private listUrl = 'getAgentList.pinpoint';
    private removeApplicationUrl = 'admin/removeApplicationName.pinpoint';
    private removeAgentUrl = 'admin/removeAgentId.pinpoint';
    private samplingRateUrl = 'agent/samplingRate.pinpoint';
    // private removeInactiveUrl = 'admin/removeInactiveAgents.pinpoint';

    constructor(
        private http: HttpClient
    ) {}

    getAgentList(appName: string): Observable<IAgentList> {
        return this.http.get<IAgentList>(this.listUrl, {
            params: new HttpParams().set('application', appName)
        });
    }

    removeApplication({applicationName, password}: {applicationName: string, password: string}): Observable<string> {
        return this.http.get(this.removeApplicationUrl, {
            params: new HttpParams()
                .set('applicationName', applicationName)
                .set('password', password),
            responseType: 'text',
        });
    }

    removeAgentId({applicationName, agentId, password}: {applicationName: string, agentId: string, password: string}): Observable<string> {
        return this.http.get(this.removeAgentUrl, {
            params: new HttpParams()
                .set('applicationName', applicationName)
                .set('agentId', agentId)
                .set('password', password),
            responseType: 'text',
        });
    }

    getSamplingRate({applicationName, agentId}: {applicationName: string, agentId: string}): Observable<any> {
        return this.http.get<string>(this.samplingRateUrl, {
            params: new HttpParams()
                .set('applicationName', applicationName)
                .set('agentId', agentId)
                .set('samplingRate', "-1")
        });
    }

    setSamplingRate({applicationName, agentId, samplingRate}: {applicationName: string, agentId: string, samplingRate: string}): Observable<any> {
        return this.http.get<string>(this.samplingRateUrl, {
            params: new HttpParams()
                .set('applicationName', applicationName)
                .set('agentId', agentId)
                .set('samplingRate', samplingRate)
        });
    }

}
