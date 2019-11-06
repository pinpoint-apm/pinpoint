import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { retry } from 'rxjs/operators';
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
        return this.http.get<IEventStatus[]>(this.requestURL, this.makeRequestOptionsArgs(from, to)).pipe(
            retry(3)
        );
    }
    private makeRequestOptionsArgs(from: number, to: number): object {
        return {
            params: new HttpParams()
                .set('agentId', this.newUrlStateNotificationService.getPathValue(UrlPathId.AGENT_ID))
                .set('from', from + '')
                .set('to', to + '')
                .set('exclude', '10199')
            //     // DESC:
            //     // [exclude] 요청에 대한 응답에서 제외하고 싶은 eventCode를 넣어줌.
        };
    }
}
