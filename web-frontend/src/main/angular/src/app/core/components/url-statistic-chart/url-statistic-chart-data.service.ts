import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

export interface IUrlStatChartDataParams {
    from: number;
    to: number;
    applicationName: string;
    agentId?: string;
    uri: string;
    type?: string;
}

@Injectable({
    providedIn: 'root'
})
export class UrlStatisticChartDataService {
    private url = 'uriStat/chart.pinpoint'

    constructor(
        private http: HttpClient,
    ) { }
    
    getData(params: IUrlStatChartDataParams): Observable<IUrlStatChartData> {
        return this.http.get<IUrlStatChartData>(this.url, this.makeRequestOptionsArgs(params));
    }

    private makeRequestOptionsArgs(params: IUrlStatChartDataParams): object {
        return {
            params: {...params}
        };
    }
}
