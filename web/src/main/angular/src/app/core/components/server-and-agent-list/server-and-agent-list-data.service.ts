import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { SortOption } from './server-and-agent-list-container.component';


const enum SortOptionParamKey {
    ID = 'AGENT_ID_ASC',
    NAME = 'AGENT_NAME_ASC',
    RECENT = 'RECENT'
}
@Injectable()
export class ServerAndAgentListDataService {
    private url = 'agents/search-application.pinpoint';

    constructor(
        private http: HttpClient,
    ) {}

    getData(applicationName: string, range: number[], sortOption: SortOption): Observable<IServerAndAgentDataV2[]> {
        return this.http.get<IServerAndAgentDataV2[]>(this.url, this.makeRequestOptionsArgs(applicationName, range, sortOption));
    }

    private makeRequestOptionsArgs(application: string, [from, to]: number[], sortOption: SortOption): object {
        return {
            params: {
                application,
                from,
                to,
                sortBy: sortOption === SortOption.ID ? SortOptionParamKey.ID
                    : sortOption === SortOption.NAME ? SortOptionParamKey.NAME
                    : SortOptionParamKey.RECENT
            }
        };
    }
}
