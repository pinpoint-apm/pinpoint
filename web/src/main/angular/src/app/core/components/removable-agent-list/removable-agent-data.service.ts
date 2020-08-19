import { Injectable } from '@angular/core';
import { HttpParams } from '@angular/common/http';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { retry } from 'rxjs/operators';


@Injectable()
export class RemovableAgentDataService {
    private listUrl = 'getAgentList.pinpoint';
    private removeApplicationUrl = 'admin/removeApplicationName.pinpoint';
    private removeAgentUrl = 'admin/removeAgentId.pinpoint';
    // private removeInactiveUrl = 'admin/removeInactiveAgents.pinpoint';

    constructor(
        private http: HttpClient
    ) {}

    getAgentList(appName: string): Observable<IAgentList> {
        return this.http.get<IAgentList>(this.listUrl, {
            params: new HttpParams().set('application', appName)
        }).pipe(
            retry(3)
        );
    }

    removeApplication({applicationName, password}: {applicationName: string, password: string}): Observable<string> {
        return this.http.get<string>(this.removeApplicationUrl, {
            params: new HttpParams()
                .set('applicationName', applicationName)
                .set('password', password)
        }).pipe(
            retry(3)
        );
    }

    removeAgentId({applicationName, agentId, password}: {applicationName: string, agentId: string, password: string}): Observable<string> {
        return this.http.get<string>(this.removeAgentUrl, {
            params: new HttpParams()
                .set('applicationName', applicationName)
                .set('agentId', agentId)
                .set('password', password)
        }).pipe(
            retry(3)
        );
    }
    // removeInactiveAgents(): Observable<string> {
    //     return this.http.get<string>(this.removeInactiveUrl).pipe(
    //         retry(3)
    //     );
    // }
}
