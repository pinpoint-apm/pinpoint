import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

@Injectable({providedIn: 'root'})
export class ApdexScoreDataService {
    private url = 'getApdexScore.pinpoint';

    constructor(
        private http: HttpClient,
    ) { }

    getApdexScore(params: {[key: string]: any}): Observable<{apdexScore: number}> {
        return this.http.get<{apdexScore: number}>(this.url, {params});
    }
}
