import { Observable } from 'rxjs';
import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';

@Injectable()
export class UrlStatisticDataService {
    private url = 'getAgentStat/uriStat/chartList.pinpoint';

    constructor(
        private http: HttpClient,
    ) {}

    getData(): Observable<any> {
        return this.http.get<any>(this.url, this.makeRequestOptionsArgs());
    }

    private makeRequestOptionsArgs(): any {
        // TODO: Deliver real param
        return {
            params: {
                agentId: 'FrontWAS2',
                from: 1614044341000,
                to: 1614045541000,
                sampleRate: 1
            }
        };
    }
}
