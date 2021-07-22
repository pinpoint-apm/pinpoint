import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable()
export class ServerAndAgentListDataService {
    private url = 'getAgentList.pinpoint';

    constructor(
        private http: HttpClient,
    ) {}

    getData(applicationName: string, range: number[]): Observable<{[key: string]: IServerAndAgentData[]}> {
        return this.http.get<{[key: string]: IServerAndAgentData[]}>(this.url, this.makeRequestOptionsArgs(applicationName, range));
    }

    private makeRequestOptionsArgs(application: string, [from, to]: number[]): object {
        return {
            params: { application, from, to }
        };
    }
}
