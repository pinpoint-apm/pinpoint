import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';

@Injectable({
    providedIn: 'root'
})
export class MetricContentsDataService {
    private url = 'systemMetric/hostGroup/host/collectedMetricInfo.pinpoint';

    constructor(
        private http: HttpClient,
    ) {}

    getMetricList(params: {hostGroup: string, host: string}): Observable<string[]> {
        // const size = Math.floor(Math.random() * 10);
        // const randomNumFunc = () => Math.floor(Math.random() * 100);

        // return of(Array(size).fill(0).map((v, i) => `metric-${randomNumFunc()}`));
        return this.http.get<string[]>(this.url, this.makeRequestOptionsArgs(params));
        // return of(['cpu', 'memoryPercent', 'memoryUsage']);
    }

    private makeRequestOptionsArgs({hostGroup, host}: {hostGroup: string, host: string}): object {
        return {
            params: {
                hostGroupName: hostGroup,
                hostName: host
            }
        };
    }
}
