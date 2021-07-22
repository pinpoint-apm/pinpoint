import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable()
export class AgentInfoDataService {
    private requestURL = 'getAgentInfo.pinpoint';

    constructor(
        private http: HttpClient
    ) {}

    getData(agentId: string, timestamp: number): Observable<IServerAndAgentData> {
        return this.http.get<IServerAndAgentData>(this.requestURL, this.makeRequestOptionsArgs(agentId, timestamp));
    }

    private makeRequestOptionsArgs(agentId: string, timestamp: number): object {
        return {
            params: new HttpParams()
                .set('agentId', agentId)
                .set('timestamp', timestamp + '')
        };
    }
}
