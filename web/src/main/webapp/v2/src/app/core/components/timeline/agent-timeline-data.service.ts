import { Injectable } from '@angular/core';
import { HttpClient, HttpParams, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError, retry } from 'rxjs/operators';

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
    requestURL = 'getAgentStatusTimeline.pinpoint';
    constructor(private http: HttpClient) { }
    getData(agentId: string, retrieveTime: IRetrieveTime): Observable<IAgentTimeline> {
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
