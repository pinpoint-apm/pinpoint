import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { IChartDataService, IChartDataFromServer } from 'app/core/components/inspector-chart/chart-data.service';
import { NewUrlStateNotificationService } from 'app/shared/services';
import { UrlPathId } from 'app/shared/models';
import { getParamForAgentChartData } from 'app/core/utils/chart-data-param-maker';

@Injectable()
export class AgentResponseTimeChartDataService implements IChartDataService {
    private requestURL = 'getAgentStat/responseTime/chart.pinpoint';

    constructor(
        private http: HttpClient,
        private newUrlStateNotificationService: NewUrlStateNotificationService,
    ) {}

    getData(range: number[]): Observable<IChartDataFromServer | AjaxException> {
        return this.http.get<IChartDataFromServer | AjaxException>(this.requestURL,
            getParamForAgentChartData(this.newUrlStateNotificationService.getPathValue(UrlPathId.AGENT_ID), range)
        );
    }
}
