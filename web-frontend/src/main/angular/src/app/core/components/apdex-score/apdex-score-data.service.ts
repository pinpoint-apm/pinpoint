import {HttpClient} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';

export interface IApdexFormulaData {
    satisfiedCount: number;
    toleratingCount: number;
    totalSamples: number;
}

@Injectable({providedIn: 'root'})
export class ApdexScoreDataService {
    private url = 'getApdexScore.pinpoint';

    constructor(
        private http: HttpClient,
    ) {
    }

    getApdexScore(params: { [key: string]: any }): Observable<{ apdexScore: number, apdexFormula: IApdexFormulaData }> {
        return this.http.get<{ apdexScore: number, apdexFormula: IApdexFormulaData }>(this.url, {params});
    }
}
