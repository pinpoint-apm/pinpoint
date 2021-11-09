import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';

@Injectable({
    providedIn: 'root'
})
export class HostGroupListDataService {
    private url = 'systemMetric/hostGroup.pinpoint';

    constructor(
        private http: HttpClient,
    ) {}

    getHostGroupList(): Observable<string[]> {
        return this.http.get<string[]>(this.url);
    }
}
