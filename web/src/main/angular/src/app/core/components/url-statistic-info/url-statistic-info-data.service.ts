import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

export interface IUrlStatInfoDataParams {
    applicationName: string;
    from: number;
    to: number;
    agentId?: string;
}

@Injectable({
    providedIn: 'root'
})
export class UrlStatisticInfoDataService {
    private url = 'uriStat/top50.pinpoint';

    constructor(
        private http: HttpClient,
    ) { }


    getData(params: IUrlStatInfoDataParams): Observable<IUrlStatInfoData> {
        return this.http.get<IUrlStatInfoData>(this.url, this.makeRequestOptionsArgs(params));
    }

    private makeRequestOptionsArgs(param: object): object {
        return {
            params: {...param}
        };
    }
}
