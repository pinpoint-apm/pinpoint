import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable, of} from 'rxjs';
import {delay} from 'rxjs/operators';

export interface IMetricDataParams {
    hostGroupName: string;
    hostName: string;
    metricDefinitionId: string;
    from: number;
    to: number;
    tags?: string;
}

@Injectable({
    providedIn: 'root'
})
export class MetricDataService {
    private metricDataUrl = 'systemMetric/hostGroup/host/collectedMetricData.pinpoint';
    private tagUrl = 'systemMetric/hostGroup/host/collectedTags.pinpoint';

    constructor(
        private http: HttpClient,
    ) {
    }

    getTagList(params: { hostGroupName: string, hostName: string, metricDefinitionId: string }): Observable<string[]> {
        return this.http.get<string[]>(this.tagUrl, this.makeRequestOptionsArgs(params));
    }

    getMetricData(params: IMetricDataParams): Observable<IMetricData> {
        return this.http.get<IMetricData>(this.metricDataUrl, this.makeRequestOptionsArgs(params));
    }

    private makeRequestOptionsArgs(param: object): object {
        return {
            params: {...param}
        };
    }
}
