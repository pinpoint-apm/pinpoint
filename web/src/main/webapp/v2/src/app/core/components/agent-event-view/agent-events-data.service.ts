import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { UrlPathId } from 'app/shared/models';
import { NewUrlStateNotificationService } from 'app/shared/services';

export interface IEventStatus {
    agentId: string;
    eventMessage?: string;
    eventTimestamp: number;
    eventTypeCode: number;
    eventTypeDesc: string;
    hasEventMessage: boolean;
    startTimestamp: number;
}

@Injectable()
export class AgentEventsDataService {
    requestURL = 'getAgentEvents.pinpoint';
    constructor(
        private http: HttpClient,
        private newUrlStateNotificationService: NewUrlStateNotificationService
    ) { }
    getData(from: number, to: number): Observable<IEventStatus[]> {
        return this.http.get<IEventStatus[]>(this.requestURL, this.makeRequestOptionsArgs(from, to));
    }
    private makeRequestOptionsArgs(from: number, to: number): object {
        return {
            params: {
                agentId: this.newUrlStateNotificationService.getPathValue(UrlPathId.AGENT_ID),
                from: from,
                to: to,
                exclude: 10199
                // DESC:
                // [exclude] 요청에 대한 응답에서 제외하고 싶은 eventCode를 넣어줌.
            }
        };
    }
}
