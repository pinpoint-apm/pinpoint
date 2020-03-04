import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { NewUrlStateNotificationService } from 'app/shared/services';
import { UrlPathId } from 'app/shared/models';
import { IInspectorChartData } from './inspector-chart-data.service';

export interface IApplicationDataSourceChart extends IInspectorChartData {
    jdbcUrl: string;
    serviceType: string;
}

@Injectable()
export class ApplicationDataSourceChartDataService {
    private requestURL = 'getApplicationStat/dataSource/chart.pinpoint';

    constructor(
        private http: HttpClient,
        private newUrlStateNotificationService: NewUrlStateNotificationService,
    ) {}

    getData(range: number[]): Observable<IApplicationDataSourceChart[] | AjaxException> {
        return this.http.get<IApplicationDataSourceChart[] | AjaxException>(this.requestURL, this.getHttpParams(range));
    }

    private getHttpParams([from, to]: number[]): object {
        return {
            params: {
                applicationId: this.newUrlStateNotificationService.getPathValue(UrlPathId.APPLICATION).getApplicationName(),
                from,
                to,
                sampleRate: 1
            }
        };
    }
}
