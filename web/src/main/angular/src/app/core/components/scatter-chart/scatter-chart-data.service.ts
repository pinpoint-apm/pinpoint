import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, of, Subject, ReplaySubject, throwError } from 'rxjs';
import { switchMap, delay, retry, filter, catchError } from 'rxjs/operators';

import { isThatType } from 'app/core/utils/util';

interface IScatterRequest {
    application: string;
    fromX: number;
    toX: number;
    groupUnitX: number;
    groupUnitY: number;
    backwardDirection: boolean;
}

@Injectable()
export class ScatterChartDataService {
    private url = 'getScatterData.pinpoint';
    private realtime = {
        interval: 2000,
        resetTimeGap: 20000
    };
    private loadStart = true;
    private requestTime: number;
    private application: string;
    private groupUnitX: number;
    private groupUnitY: number;
    private innerDataRequest = new Subject<IScatterRequest>();
    private innerRealTimeDataRequest = new Subject<IScatterRequest>();
    private outScatterData = new Subject<IScatterData>();
    private savedScatterData = new ReplaySubject<IScatterData>();
    private outRealTimeScatterData = new Subject<IScatterData>();
    private outScatterErrorData = new Subject<IServerErrorFormat>();
    private outRealTimeScatterErrorData = new Subject<IServerErrorFormat>();
    private outReset = new Subject<void>();

    outScatterData$: Observable<IScatterData>;
    outScatterErrorData$: Observable<IServerErrorFormat>;
    outRealTimeScatterData$: Observable<IScatterData>;
    outRealTimeScatterErrorData$: Observable<IServerErrorFormat>;
    savedScatterData$: Observable<IScatterData>;
    onReset$: Observable<void>;

    constructor(
        private http: HttpClient
    ) {
        this.outScatterData$ = this.outScatterData.asObservable();
        this.outScatterErrorData$ = this.outScatterErrorData.asObservable();
        this.outRealTimeScatterData$ = this.outRealTimeScatterData.asObservable();
        this.outRealTimeScatterErrorData$ = this.outRealTimeScatterErrorData.asObservable();
        this.savedScatterData$ = this.savedScatterData.asObservable();
        this.onReset$ = this.outReset.asObservable();
        this.connectDataRequest();
    }

    private connectDataRequest(): void {
        this.innerDataRequest.pipe(
            filter(() => this.loadStart),
            switchMap((params: IScatterRequest) => {
                return this.requestHttp(params).pipe(
                    retry(3),
                    switchMap((res: IScatterData | IServerErrorFormat) => isThatType(res, 'exception') ? throwError(res) : of(res))
                );
            })
        ).subscribe((scatterData: IScatterData) => {
            this.subscribeStaticRequest(scatterData);
        }, (error: IServerErrorFormat) => {
            this.outScatterErrorData.next(error);
        });
        this.innerRealTimeDataRequest.pipe(
            filter(() => this.loadStart),
            switchMap((params: IScatterRequest) => {
                return this.requestHttp(params).pipe(
                    retry(3),
                    catchError((error: IServerErrorFormat) => of(error)),
                    filter((res: IScatterData | IServerErrorFormat) => {
                        if (isThatType(res, 'exception')) {
                            this.outReset.next();
                            return false;
                        } else {
                            return true;
                        }
                    })
                );
            }),
        ).subscribe((scatterData: IScatterData) => {
            this.subscribeRealTimeRequest(scatterData);
        }, (error: IServerErrorFormat) => {
            // this.outScatterErrorData.next(error);
        });
    }
    private requestHttp(params: IScatterRequest): Observable<IScatterData> {
        return this.http.get<IScatterData>(this.url, this.makeRequestOptionsArgs(
            params.application,
            params.fromX,
            params.toX,
            params.groupUnitX,
            params.groupUnitY,
            params.backwardDirection)
        );
    }
    private getData(fromX: number, toX: number, backwardDirection: boolean): void {
        this.requestTime = Date.now();
        const params = {
            application: this.application,
            fromX: fromX,
            toX: toX,
            groupUnitX: this.groupUnitX,
            groupUnitY: this.groupUnitY,
            backwardDirection: backwardDirection
        };
        return this.innerDataRequest.next(params);
    }
    private getRealTimeData(fromX: number, toX: number, backwardDirection: boolean): void {
        this.requestTime = Date.now();
        const params = {
            application: this.application,
            fromX: fromX,
            toX: toX,
            groupUnitX: this.groupUnitX,
            groupUnitY: this.groupUnitY,
            backwardDirection: backwardDirection
        };
        return this.innerRealTimeDataRequest.next(params);
    }
    loadData(application: string, fromX: number, toX: number, groupUnitX: number, groupUnitY: number, initLastData?: boolean): void {
        this.loadStart = true;
        this.application = application;
        this.groupUnitX = groupUnitX;
        this.groupUnitY = groupUnitY;
        if (initLastData !== false) {
            this.savedScatterData.complete();
            this.savedScatterData = new ReplaySubject<IScatterData>();
            this.savedScatterData$ = this.savedScatterData.asObservable();
        }
        this.getData(fromX, toX, true);
    }
    private subscribeStaticRequest(scatterData: IScatterData): void {
        this.savedScatterData.next(scatterData);
        this.outScatterData.next(scatterData);
    }
    getSavedData(): Observable<IScatterData> {
        return this.savedScatterData$;
    }
    loadRealTimeData(application: string, fromX: number, toX: number, groupUnitX: number, groupUnitY: number): void {
        this.loadStart = true;
        this.application = application;
        this.groupUnitX = groupUnitX;
        this.groupUnitY = groupUnitY;
        // this.getData(fromX, toX, false, true);
    }
    loadRealTimeDataV2(toX: number): void {
        this.loadStart = true;
        of(1).pipe(delay(this.realtime.interval)).subscribe((useless: number) => {
            this.getRealTimeData(toX, toX + this.realtime.interval, false);
        });
    }
    stopLoad(): void {
        this.loadStart = false;
    }
    isConnected(): boolean {
        return this.loadStart;
    }
    private subscribeRealTimeRequest(scatterData: IScatterData): void {
        const roundTripTime = Date.now() - this.requestTime;
        let fromNext = 0;
        let toNext = 0;
        let delayTime = this.realtime.interval - roundTripTime;

        if (scatterData.complete) {
            fromNext = scatterData.to;
            toNext = fromNext + this.realtime.interval;
            if (delayTime > 0) {
                // When the response arrives on time
                const timeGapInterServerAndClient = scatterData.currentServerTime - toNext;
                // if (timeGapInterServerAndClient >= delayTime) {
                    if (timeGapInterServerAndClient >= this.realtime.interval) {
                        delayTime = 0;
                    } else {
                        // delayTime = Math.min(Math.abs(timeGapInterServerAndClient), delayTime);
                    }
                // }
            } else {
                delayTime = 0;
            }
        } else {
            fromNext = scatterData.resultTo;
            toNext = scatterData.to;
            delayTime = 0;
        }

        if (scatterData.currentServerTime - toNext >= this.realtime.resetTimeGap) {
            this.outReset.next();
        } else {
            this.outRealTimeScatterData.next(scatterData);
            of(1).pipe(delay(delayTime)).subscribe((useless: number) => {
                this.getRealTimeData(fromNext, toNext, false);
            });
        }
    }
    private makeRequestOptionsArgs(application: string, fromX: number, toX: number, groupUnitX: number, groupUnitY: number, backwardDirection: boolean): object {
        return {
            params: new HttpParams()
                .set('application', application)
                .set('from', fromX + '')
                .set('to', toX + '')
                .set('limit', '5000')
                .set('filter', '')
                .set('xGroupUnit', groupUnitX + '')
                .set('yGroupUnit', groupUnitY + '')
                .set('backwardDirection', backwardDirection + '')
        };
    }
}
