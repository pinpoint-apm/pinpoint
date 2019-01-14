import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { shareReplay } from 'rxjs/operators';

import { IChartDataService, IChartDataFromServer } from 'app/core/components/inspector-chart/chart-data.service';
import { NewUrlStateNotificationService } from 'app/shared/services';
import { UrlPathId } from 'app/shared/models';
import { getParamForAgentChartData } from 'app/core/utils/chart-data-param-maker';

@Injectable()
export class TransactionViewMemoryChartDataService implements IChartDataService {
    private requestURL = 'getAgentStat/jvmGc/chart.pinpoint';
    private cache$: Observable<any>;

    constructor(
        private http: HttpClient,
        private newUrlStateNotificationService: NewUrlStateNotificationService,
    ) {}

    getData(range: number[]): Observable<IChartDataFromServer | AjaxException> {
        if (!this.cache$) {
            const httpRequest$ = this.http.get<IChartDataFromServer | AjaxException>(this.requestURL,
                getParamForAgentChartData(this.newUrlStateNotificationService.getPathValue(UrlPathId.AGENT_ID), range));

            this.cache$ = httpRequest$.pipe(
                shareReplay(1)
            );
        }

        return this.cache$;
    }
}
