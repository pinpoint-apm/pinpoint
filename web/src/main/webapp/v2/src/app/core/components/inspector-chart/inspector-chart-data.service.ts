import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { NewUrlStateNotificationService } from 'app/shared/services';
import { UrlPathId } from 'app/shared/models';

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

    getData(url: string, range: number[]): Observable<IInspectorChartData | AjaxException> {
        return this.http.get<IInspectorChartData | AjaxException>(url, this.getHttpParams(range));
    }

    private getHttpParams([from, to]: number[]): object {
        const isAgentPage = this.newUrlStateNotificationService.hasValue(UrlPathId.AGENT_ID);
        const idObj = isAgentPage
            ? { agentId: this.newUrlStateNotificationService.getPathValue(UrlPathId.AGENT_ID) }
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
