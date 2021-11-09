import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';

@Injectable({
    providedIn: 'root'
})
export class HostGroupAndHostListDataService {
    private url = 'systemMetric/hostGroup/host.pinpoint';

    constructor(
        private http: HttpClient,
    ) {}

    getHostList(hostGroup: string): Observable<string[]> {
        return this.http.get<string[]>(this.url, this.makeRequestOptionsArgs(hostGroup));
    }

    private makeRequestOptionsArgs(hostGroup: string): object {
        return {
            params: {
                hostGroupName: hostGroup
            }
        };
    }
}
