import { Injectable } from "@angular/core";
import { HttpClient, HttpParams } from "@angular/common/http";
import { Observable } from "rxjs";

import { SortOption } from "./server-and-agent-list-container.component";

const enum SortOptionParamKey {
  ID = "AGENT_ID_ASC",
  NAME = "AGENT_NAME_ASC",
  RECENT = "RECENT",
}

@Injectable({ providedIn: "root" })
export class ServerAndAgentListDataService {
  private url = "api/agents/search-application";

  // TODO: Agent-list fetch service 일원화
  constructor(private http: HttpClient) {}

  getData(
    applicationName: string,
    range: number[],
    sortOption: SortOption = SortOption.ID,
    serviceTypeName?: string,
    serviceTypeCode?: number,
    applicationPairs?: string,
  ): Observable<IServerAndAgentDataV2[]> {
    return this.http.get<IServerAndAgentDataV2[]>(
      this.url,
      this.makeRequestOptionsArgs(
        applicationName,
        range,
        sortOption,
        serviceTypeName,
        serviceTypeCode,
        applicationPairs,
      )
    );
  }

  private makeRequestOptionsArgs(
    application: string,
    [from, to]: number[],
    sortOption: SortOption,
    serviceTypeName: string,
    serviceTypeCode: number,
    applicationPairs: string,
  ): object {
    const sortBy = sortOption === SortOption.ID
      ? SortOptionParamKey.ID
      : sortOption === SortOption.NAME
      ? SortOptionParamKey.NAME
      : SortOptionParamKey.RECENT
    
    let params = new HttpParams()
      .set('application', application)
      .set('serviceTypeName', serviceTypeName)
      .set('from', `${from}`)
      .set('to', `${to}`)
      .set('sortBy', sortBy)

    if (serviceTypeCode) {
      params = params.set('serviceTypeCode', `${serviceTypeCode}`)
    }

    if (applicationPairs) {
      params = params.set('applicationPairs', applicationPairs)
    }

    return {params}
  }
}
