import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { IInspectorChartData } from './inspector-chart-data.service';
import { NewUrlStateNotificationService } from 'app/shared/services';
import { UrlPathId } from 'app/shared/models';

export interface IAgentDataSourceChart extends IInspectorChartData {
    databaseName: string;
    id: number;
    jdbcUrl: string;
    serviceType: string;
}

@Injectable()
export class AgentDataSourceChartDataService {
    private requestURL = 'getAgentStat/dataSource/chartList.pinpoint';

    constructor(
        private http: HttpClient,
        private newUrlStateNotificationService: NewUrlStateNotificationService,
    ) {}

    getData(range: number[]): Observable<IAgentDataSourceChart[] | AjaxException> {
        return this.http.get<IAgentDataSourceChart[] | AjaxException>(this.requestURL, this.getHttpParams(range));
    }

    private getHttpParams([from, to]: number[]): object {
        return {
            params: {
                agentId: this.newUrlStateNotificationService.getPathValue(UrlPathId.AGENT_ID),
                from,
                to,
                sampleRate: 1
            }
        };
    }
}
