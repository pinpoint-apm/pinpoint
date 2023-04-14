import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {tap} from 'rxjs/operators';

@Injectable()
export class AgentStatisticDataService {
    private url = 'agents/statistics.pinpoint';
    private lastRequestTime: number;

    constructor(
        private http: HttpClient
    ) {
    }

    getData(): Observable<IServerAndAgentDataV2[]> {
        return this.http.get<IServerAndAgentDataV2[]>(this.url).pipe(
            tap(() => {
                this.lastRequestTime = new Date().getTime();
            })
        );
    }

    getLastRequestTime(): number {
        return this.lastRequestTime;
    }
}
