import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { tap } from 'rxjs/operators';

@Injectable()
export class AgentStatisticDataService {
    private url = 'getAgentList.pinpoint';
    private lastRequestTime: number;

    constructor(
        private http: HttpClient
    ) {}

    getData(): Observable<IAgentList> {
        return this.http.get<IAgentList>(this.url).pipe(
            tap(() => {
                this.lastRequestTime = new Date().getTime();
            })
        );
    }

    getLastRequestTime(): number {
        return this.lastRequestTime;
    }
}
