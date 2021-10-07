import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { NewUrlStateNotificationService } from 'app/shared/services';
import { UrlPathId, UrlQuery } from 'app/shared/models';

export interface IInspectorChartData {
    charts: {
        schema: {
            [key: string]: string[] | string;
        },
        x: number[];
        y: {
            [key: string]: (number | string)[][];
        }
    };
}

@Injectable()
export class InspectorChartDataService {
    constructor(
        private http: HttpClient,
        private newUrlStateNotificationService: NewUrlStateNotificationService
    ) {}

    getData(url: string, range: number[]): Observable<IInspectorChartData> {
        return this.http.get<IInspectorChartData>(url, this.getHttpParams(range));
    }

    // TODO: Include agent/application info in parameter along with range
    private getHttpParams([from, to]: number[]): object {
        const isAgentPage = this.newUrlStateNotificationService.hasValue(UrlPathId.AGENT_ID) ||
            this.newUrlStateNotificationService.hasValue(UrlQuery.TRANSACTION_INFO);

        const idObj = isAgentPage
            ? { agentId: this.newUrlStateNotificationService.getPathValue(UrlPathId.AGENT_ID) || JSON.parse(this.newUrlStateNotificationService.getQueryValue(UrlQuery.TRANSACTION_INFO)).agentId }
            : { applicationId : this.newUrlStateNotificationService.getPathValue(UrlPathId.APPLICATION).getApplicationName() };

        return {
            params: {
                ...idObj,
                from,
                to,
                sampleRate: 1
            }
        };
    }
}
