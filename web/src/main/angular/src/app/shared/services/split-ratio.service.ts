import { Injectable } from '@angular/core';
import { UrlPath } from 'app/shared/models';

@Injectable()
export class SplitRatioService {
    private splitRatioMap = new Map<string, number[]>();

    constructor() {
        this.initSplitRatioMap();
    }

    private initSplitRatioMap(): void {
        this.splitRatioMap.set(UrlPath.TRANSACTION_LIST, [30, 70]);
        this.splitRatioMap.set(UrlPath.TRANSACTION_VIEW, [40, 60]);
    }

    getSplitRatio(view: string): number[] {
        return this.splitRatioMap.get(view);
    }
}
