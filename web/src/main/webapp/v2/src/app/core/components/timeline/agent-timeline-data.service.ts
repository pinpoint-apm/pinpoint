import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { retry } from 'rxjs/operators';

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

@Injectable()
export class AgentTimelineDataService {
    private requestURL = 'getAgentStatusTimeline.pinpoint';

    constructor(
        private http: HttpClient
    ) {}

    getData(agentId: string, range: number[]): Observable<IAgentTimeline> {
        return this.http.get<IAgentTimeline>(this.requestURL, this.makeRequestOptionsArgs(agentId, range)).pipe(
            retry(3)
        );
    }

    private makeRequestOptionsArgs(agentId: string, [from, to]: number[]): { 'params': { [key: string]: any } } {
        return {
            params: new HttpParams()
                .set('agentId', agentId)
                .set('from', from + '')
                .set('to', to + '')
                .set('exclude', 10199 + '')
        };
    }
}
