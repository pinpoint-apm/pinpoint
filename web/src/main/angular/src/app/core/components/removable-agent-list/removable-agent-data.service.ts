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

    constructor(private http: HttpClient) {}
    getAgentList(appName: string): Observable<IAgentList> {
        return this.http.get<IAgentList>(this.listUrl, {
            params: new HttpParams().set('application', appName)
        }).pipe(
            retry(3)
        );
    }
    removeApplication(applicationName: string): Observable<string> {
        return this.http.get<string>(this.removeApplicationUrl, {
            params: new HttpParams().set('applicationName', applicationName)
        }).pipe(
            retry(3)
        );
    }
    removeAgentId({applicationName, agentId}: {applicationName: string, agentId: string}): Observable<string> {
        return this.http.get<string>(this.removeAgentUrl, {
            params: new HttpParams().set('applicationName', applicationName).set('agentId', agentId)
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
