import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';

import {SortOption} from './server-and-agent-list-container.component';


const enum SortOptionParamKey {
    ID = 'AGENT_ID_ASC',
    NAME = 'AGENT_NAME_ASC',
    RECENT = 'RECENT'
}

@Injectable({providedIn: 'root'})
export class ServerAndAgentListDataService {
    private url = 'agents/search-application.pinpoint';

    // TODO: Agent-list fetch service 일원화
    constructor(
        private http: HttpClient,
    ) {
    }

    getData(applicationName: string, range: number[], sortOption: SortOption = SortOption.ID): Observable<IServerAndAgentDataV2[]> {
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
