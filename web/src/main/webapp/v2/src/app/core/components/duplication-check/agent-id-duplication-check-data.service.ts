import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface IAgentIdAvailable {
    code: number;
    message: string;
}

@Injectable()
export class AgentIdDuplicationCheckDataService {
    private requestURL = 'isAvailableAgentId.pinpoint';

    constructor(
        private http: HttpClient,
    ) {}

    getResponseWithParams(value: string): Observable<IAgentIdAvailable> {
        return this.http.get<IAgentIdAvailable>(this.requestURL, {
            params: new HttpParams().set('agentId', value)
        });
    }
}
