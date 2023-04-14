import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable, of} from 'rxjs';

export interface IMetricInfo {
    metricDefinitionId: string;
    tagGroup: boolean;
}

@Injectable({
    providedIn: 'root'
})
export class MetricContentsDataService {
    private url = 'systemMetric/hostGroup/host/collectedMetricInfoV2.pinpoint';

    constructor(
        private http: HttpClient,
    ) {
    }

    getMetricList(params: { hostGroup: string, host: string }): Observable<IMetricInfo[]> {
        return this.http.get<IMetricInfo[]>(this.url, this.makeRequestOptionsArgs(params));
    }

    private makeRequestOptionsArgs({hostGroup, host}: { hostGroup: string, host: string }): object {
        return {
            params: {
                hostGroupName: hostGroup,
                hostName: host
            }
        };
    }
}
