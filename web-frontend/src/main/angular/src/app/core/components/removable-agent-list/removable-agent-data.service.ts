import {Injectable} from '@angular/core';
import {HttpParams} from '@angular/common/http';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';

@Injectable()
export class RemovableAgentDataService {
    private listUrl = 'agents/search-application.pinpoint';
    private removeApplicationUrl = 'admin/removeApplicationName.pinpoint';
    private removeAgentUrl = 'admin/removeAgentId.pinpoint';

    constructor(
        private http: HttpClient
    ) {
    }

    getAgentList(appName: string): Observable<IServerAndAgentDataV2[]> {
        return this.http.get<IServerAndAgentDataV2[]>(this.listUrl, {
            params: new HttpParams().set('application', appName)
        });
    }

    removeApplication({applicationName, password}: { applicationName: string, password: string }): Observable<string> {
        return this.http.get(this.removeApplicationUrl, {
            params: new HttpParams()
                .set('applicationName', applicationName)
                .set('password', password),
            responseType: 'text',
        });
    }

    removeAgentId({
                      applicationName,
                      agentId,
                      password
                  }: { applicationName: string, agentId: string, password: string }): Observable<string> {
        return this.http.get(this.removeAgentUrl, {
            params: new HttpParams()
                .set('applicationName', applicationName)
                .set('agentId', agentId)
                .set('password', password),
            responseType: 'text',
        });
    }
}
