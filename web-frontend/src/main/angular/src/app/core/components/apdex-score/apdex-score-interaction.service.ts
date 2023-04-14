import {Injectable} from '@angular/core';
import {BehaviorSubject, Observable} from 'rxjs';

import {IApdexFormulaData} from './apdex-score-data.service';

@Injectable({providedIn: 'root'})
export class ApdexScoreInteractionService {
    private outApdexFormulaData = new BehaviorSubject<IApdexFormulaData>(null);

    onApdexFormulaData$: Observable<IApdexFormulaData>;

    constructor() {
        this.onApdexFormulaData$ = this.outApdexFormulaData.asObservable();
    }

    setApdexFormulaData(apdexFormulaData: IApdexFormulaData): void {
        this.outApdexFormulaData.next(apdexFormulaData);
    }
}
