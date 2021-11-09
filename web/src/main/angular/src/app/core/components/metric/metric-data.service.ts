import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { delay } from 'rxjs/operators';

@Injectable({
    providedIn: 'root'
})
export class MetricDataService {
    private url = 'systemMetric/hostGroup/host/collectedMetricData.pinpoint';

    constructor(
        private http: HttpClient,
    ) {}

    getMetricData(params: object): Observable<IMetricData> {
        const startTime = params['from'];
        const interval = 5000;
        const size = (params['to'] - params['from']) / interval + 1;

        const timestamp = Array(size).fill(0).map((v, i) => startTime + interval * i);
        const data = [300, 200, 100, 50, 30, 20, 10, 10]
            .map(v => Array(size)
                .fill(0)
                .map(() => Math.round(Math.random() * v))
            );

        // return of({
        //     title: 'CPU',
        //     timestamp,
        //     metricValueGroups: [
        //         {
        //             groupName: 'disk count (device:xvda1, fstype:xfs, mode:rw, path:/})',
        //             metricValues: [
        //                 {
        //                     fieldName: 'Free11111111dasdasdasdasdas',
        //                     values: data[Math.floor(data.length * Math.random())]
        //                 },
        //                 {
        //                     fieldName: 'Total231323123123',
        //                     values: data[Math.floor(data.length * Math.random())]
        //                 },
        //                 {
        //                     fieldName: 'Used313123123123213123',
        //                     values: data[Math.floor(data.length * Math.random())]
        //                 }
        //             ]
        //         },
        //         {
        //             groupName: 'disk count (device:xvda3, fstype:xfs, mode:rw, path:/home1})',
        //             metricValues: [
        //                 {
        //                     fieldName: 'Free',
        //                     values: data[Math.floor(data.length * Math.random())]
        //                 },
        //                 {
        //                     fieldName: 'Total',
        //                     values: data[Math.floor(data.length * Math.random())]
        //                 },
        //                 {
        //                     fieldName: 'Used',
        //                     values: data[Math.floor(data.length * Math.random())]
        //                 }
        //             ]
        //         },
        //         {
        //             groupName: 'disk count (device:xvda3, fstype:xfs, mode:rw, path:/home})',
        //             metricValues: [
        //                 {
        //                     fieldName: 'Free',
        //                     values: data[Math.floor(data.length * Math.random())]
        //                 },
        //                 {
        //                     fieldName: 'Total',
        //                     values: data[Math.floor(data.length * Math.random())]
        //                 },
        //                 {
        //                     fieldName: 'Used',
        //                     values: data[Math.floor(data.length * Math.random())]
        //                 }
        //             ]
        //         }
        //     ]
        // }).pipe(
        //     delay(200)
        // );
        // return of({
        //     title: 'CPU',
        //     timestamp,

        //     metricValues: [
        //         {
        //             fieldName: 'Field-1',
        //             values: data[Math.floor(data.length * Math.random())]
        //         },
        //         {
        //             fieldName: 'Field-2',
        //             values: data[Math.floor(data.length * Math.random())]
        //         }
        //     ]
        // });
        return this.http.get<IMetricData>(this.url, this.makeRequestOptionsArgs(params));
    }

    // hostGroupId=hyunjoon.cho&hostName=dev-pinpoint-olap02-ncl&metricName=cpu&metricDefinitionId=cpu&from=1626767520000&to=1626768020000
    // hostGroupId=minwooApp&hostName=heaven&metricName=mem&metricDefinitionId=memoryPercent&from=1628232054419&to=1628232454419
    // hostGroupId=hyunjoon.cho&hostName=dev-pinpoint-olap02-ncl&metricName=mem&metricDefinitionId=memoryUsage&from=1626767520000&to=1626768020000

    private makeRequestOptionsArgs(param: object): object {
        return {
            params: {...param}
        };
    }
}
