import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

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
        return this.http.get<IAgentTimeline>(this.requestURL, this.makeRequestOptionsArgs(agentId, retrieveTime));
    }
    private makeRequestOptionsArgs(agentId: string, { start: from, end: to }: IRetrieveTime): { 'params': { [key: string]: any } } {
        return {
            params: {
                agentId,
                from,
                to,
                exclude: 10199
                // DESC:
                // [exclude] 요청에 대한 응답에서 제외하고 싶은 eventCode를 넣어줌.
            }
        };
    }
}
