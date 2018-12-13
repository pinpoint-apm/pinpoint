import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { retry } from 'rxjs/operators';

import { NewUrlStateNotificationService } from 'app/shared/services';
import { UrlPathId } from 'app/shared/models';

export interface IAgentTimeline {
    agentEventTimeline: {
        timelineSegments: any
    };
    agentStatusTimeline: {
        includeWarning: boolean;
        timelineSegments: {
            endTimestamp: number;
            startTimestamp: number;
            value: string;
        }[]
    };
}
export interface IRetrieveTime {
    start: number;
    end: number;
}

@Injectable()
export class AgentTimelineDataService {
    private requestURL = 'getAgentStatusTimeline.pinpoint';

    constructor(
        private http: HttpClient,
        private newUrlStateNotificationService: NewUrlStateNotificationService
    ) {}
    getData(retrieveTime: IRetrieveTime): Observable<IAgentTimeline> {
        const agentId = this.newUrlStateNotificationService.getPathValue(UrlPathId.AGENT_ID);

        return this.http.get<IAgentTimeline>(this.requestURL, this.makeRequestOptionsArgs(agentId, retrieveTime)).pipe(
            retry(3)
        );
    }

    private makeRequestOptionsArgs(agentId: string, { start: from, end: to }: IRetrieveTime): { 'params': { [key: string]: any } } {
        return {
            params: new HttpParams()
                .set('agentId', agentId)
                .set('from', from + '')
                .set('to', to + '')
                .set('exclude', 10199 + '')
        };
    }
}
