import { Injectable } from '@angular/core';
import { BehaviorSubject, Subject, Observable } from 'rxjs';

export interface IChangedAgentParam {
    instanceKey: string;
    agent: string;
}
export interface IChangedViewTypeParam {
    instanceKey: string;
    name: string;
    checked: boolean;
}
export interface IRangeParam {
    instanceKey: string;
    from: number;
    to: number;
}
export interface IResetParam {
    instanceKey: string;
    application: string;
    agent: string;
    from: number;
    to: number;
    mode: string;
}

@Injectable()
export class ScatterChartInteractionService {

    private outChartData = new BehaviorSubject<{instanceKey: string, data: IScatterData}>({instanceKey: '', data: null});
    public onChartData$: Observable<{instanceKey: string, data: IScatterData}>;

    private outViewType = new Subject<IChangedViewTypeParam>();
    public onViewType$: Observable<IChangedViewTypeParam>;

    private outChangeYRange = new Subject<IRangeParam>();
    public onChangeYRange$: Observable<IRangeParam>;

    private outSelectedAgent = new Subject<IChangedAgentParam>();
    public onSelectedAgent$: Observable<IChangedAgentParam>;

    private outInvokeDownloadChart = new Subject<string>();
    public onInvokeDownloadChart$: Observable<string>;

    private outReset = new Subject<IResetParam>();
    public onReset$: Observable<IResetParam>;

    private outError = new Subject<IServerErrorFormat>();
    public onError$: Observable<IServerErrorFormat>;
    constructor() {
        this.onChartData$ = this.outChartData.asObservable();
        this.onViewType$ = this.outViewType.asObservable();
        this.onChangeYRange$ = this.outChangeYRange.asObservable();
        this.onSelectedAgent$ = this.outSelectedAgent.asObservable();
        this.onInvokeDownloadChart$ = this.outInvokeDownloadChart.asObservable();
        this.onReset$ = this.outReset.asObservable();
        this.onError$ = this.outError.asObservable();
    }
    addChartData(instanceKey: string, data: IScatterData): void {
        this.outChartData.next({
            instanceKey: instanceKey,
            data: data
        });
    }
    changeViewType(params: IChangedViewTypeParam): void {
        this.outViewType.next(params);
    }
    changeYRange(yRange: IRangeParam): void {
        this.outChangeYRange.next(yRange);
    }
    changeAgent(instanceKey: string, agent: string): void {
        this.outSelectedAgent.next({
            instanceKey: instanceKey,
            agent: agent
        });
    }
    downloadChart(instanceKey: string): void {
        this.outInvokeDownloadChart.next(instanceKey);
    }
    reset(instanceKey: string, application: string, agent: string, from: number, to: number, mode: string): void {
        this.outReset.next({
            instanceKey,
            application,
            agent,
            from,
            to,
            mode,
        });
    }
    setError(error: IServerErrorFormat): void {
        this.outError.next(error);
    }
}
