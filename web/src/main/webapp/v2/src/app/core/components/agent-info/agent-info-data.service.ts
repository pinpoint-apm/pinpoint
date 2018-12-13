import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { retry } from 'rxjs/operators';

import { NewUrlStateNotificationService } from 'app/shared/services';
import { UrlPathId } from 'app/shared/models';

@Injectable()
export class AgentInfoDataService {
    private requestURL = 'getAgentInfo.pinpoint';

    constructor(
        private http: HttpClient,
        private newUrlStateNotificationService: NewUrlStateNotificationService
    ) {}
    getData(timestamp: number): Observable<IServerAndAgentData> {
        const agentId = this.newUrlStateNotificationService.getPathValue(UrlPathId.AGENT_ID);

        return this.http.get<IServerAndAgentData>(this.requestURL, this.makeRequestOptionsArgs(agentId, timestamp)).pipe(
            retry(3)
        );
    }
    private makeRequestOptionsArgs(agentId: string, timestamp: number): object {
        return {
            params: new HttpParams()
                .set('agentId', agentId)
                .set('timestamp', timestamp + '')
        };
    }
}
