import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of, Subject } from 'rxjs';
import { switchMap, delay } from 'rxjs/operators';
import { ScatterChart } from 'app/core/components/scatter-chart/class/scatter-chart.class';

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
        // interval: 5000,
        resetTimeGap: 20000
    };
    private currentMode: string;
    private lastScatterData: IScatterData[] = [];
    private requestTime: number;
    private application: string;
    private groupUnitX: number;
    private groupUnitY: number;
    private innerDataRequest = new Subject<IScatterRequest>();
    private innerDataRequest$: Observable<IScatterRequest>;
    private outScatterData = new Subject<IScatterData>();
    outScatterData$: Observable<IScatterData>;
    constructor(private http: HttpClient) {
        this.innerDataRequest$ = this.innerDataRequest.asObservable();
        this.outScatterData$ = this.outScatterData.asObservable();
        this.connectDataRequest();
    }
    private connectDataRequest(): void {
        this.innerDataRequest$.pipe(
            switchMap((params: IScatterRequest) => {
                return this.http.get<IScatterData>(this.url, this.makeRequestOptionsArgs(
                    params.application,
                    params.fromX,
                    params.toX,
                    params.groupUnitX,
                    params.groupUnitY,
                    params.backwardDirection)
                );
            })
        ).subscribe((scatterData: IScatterData) => {
            if ( this.currentMode === ScatterChart.MODE.REALTIME) {
                this.subscribeRealTimeRequest(scatterData);
            } else {
                this.subscribeStaticRequest(scatterData);
            }
        });
    }
    private getData(fromX: number, toX: number, backwardDirection: boolean): void {
        this.requestTime = Date.now();
        return this.innerDataRequest.next({
            application: this.application,
            fromX: fromX,
            toX: toX,
            groupUnitX: this.groupUnitX,
            groupUnitY: this.groupUnitY,
            backwardDirection: backwardDirection
        });
    }
    loadData(application: string, fromX: number, toX: number, groupUnitX: number, groupUnitY: number, initLastData?: boolean): void {
        this.application = application;
        this.groupUnitX = groupUnitX;
        this.groupUnitY = groupUnitY;
        this.lastScatterData = initLastData === false ? this.lastScatterData : [];
        this.getData(fromX, toX, true);
    }
    private subscribeStaticRequest(scatterData: IScatterData): void {
        this.lastScatterData.push(scatterData);
        this.outScatterData.next(scatterData);
    }
    loadLastData(): IScatterData[] {
        return this.lastScatterData;
    }
    loadRealTimeData(application: string, fromX: number, toX: number, groupUnitX: number, groupUnitY: number): void {
        this.application = application;
        this.groupUnitX = groupUnitX;
        this.groupUnitY = groupUnitY;
        this.getData(fromX, toX, false);
    }
    setCurrentMode(mode: string): void {
        this.currentMode = mode;
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
                const timeGapInterServerAndClient = scatterData.currentServerTime - toNext;
                if (timeGapInterServerAndClient >= delayTime) {
                    if ( timeGapInterServerAndClient >= this.realtime.interval ) {
                        delayTime = 0;
                    } else {
                        delayTime = Math.min(Math.abs(timeGapInterServerAndClient), delayTime);
                    }
                }
            } else {
                delayTime = 0;
            }
        } else {
            // TODO: 처리해야 함.
            fromNext = scatterData.from;
            toNext = scatterData.resultFrom;
            delayTime = 0;
        }
        if (scatterData.currentServerTime - toNext >= this.realtime.resetTimeGap) {
            scatterData.reset = true;
            this.outScatterData.next(scatterData);
        } else {
            this.outScatterData.next(scatterData);
            of(1).pipe(delay(delayTime)).subscribe((useless: number) => {
                this.getData(fromNext, toNext, false);
            });
        }
    }
    private makeRequestOptionsArgs(application: string, fromX: number, toX: number, groupUnitX: number, groupUnitY: number, backwardDirection: boolean): object {
        return {
            params: {
                application: application,
                from: fromX,
                to: toX,
                limit: 5000,
                filter: '',
                xGroupUnit: groupUnitX,
                yGroupUnit: groupUnitY,
                backwardDirection: backwardDirection
            }
        };
    }
}
