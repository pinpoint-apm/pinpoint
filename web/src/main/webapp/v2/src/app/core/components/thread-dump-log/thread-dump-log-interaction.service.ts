import { Injectable } from '@angular/core';
import { Subject, Observable } from 'rxjs';

export interface IParam {
    threadName: string;
    localTraceId: number;
}

@Injectable()
export class ThreadDumpLogInteractionService {
    private outParam: Subject<IParam> = new Subject();
    onParam$: Observable<IParam>;
    constructor() {
        this.onParam$ = this.outParam.asObservable();
    }
    sendParam(param: IParam): void {
        this.outParam.next(param);
    }
}

