import { Injectable } from '@angular/core';
import { Subject, Observable, of } from 'rxjs';
import { map, filter, tap } from 'rxjs/operators';

import { Actions } from 'app/shared/store';
import { WebAppSettingDataService, StoreHelperService } from 'app/shared/services';

export enum SOURCE_TYPE {
    APPLICATION_INSPECTOR = 'APPLICATION_INSPECTOR',
    AGENT_INSPECTOR = 'AGENT_INSPECTOR'
}

@Injectable()
export class InspectorChartListDataService {
    private unsubscribe: Subject<null> = new Subject();
    private chartInfo: {
        [SOURCE_TYPE.APPLICATION_INSPECTOR]: IChartLayoutInfoResponse,
        [SOURCE_TYPE.AGENT_INSPECTOR]: IChartLayoutInfoResponse,
    } = {
        [SOURCE_TYPE.APPLICATION_INSPECTOR]: null,
        [SOURCE_TYPE.AGENT_INSPECTOR]: null,
    };
    private applicationChartVisibleState: Observable<{[key: string]: boolean}>;
    private agentChartVisibleState: Observable<{[key: string]: boolean}>;
    constructor(
        private storeHelperService: StoreHelperService,
        private webAppSettingDataService: WebAppSettingDataService
    ) {
        this.applicationChartVisibleState = this.storeHelperService.getApplicationInspectorChartLayoutInfo(this.unsubscribe).pipe(
            filter((data: IChartLayoutInfoResponse) => {
                return data !== null && data !== undefined;
            }),
            tap((data: IChartLayoutInfoResponse) => {
                this.chartInfo[SOURCE_TYPE.APPLICATION_INSPECTOR] = data;
            }),
            map((data: IChartLayoutInfoResponse) => {
                return this.extractVisibleState(data[Object.keys(data)[0]], SOURCE_TYPE.APPLICATION_INSPECTOR);
            })
        );
        this.agentChartVisibleState = this.storeHelperService.getAgentInspectorChartLayoutInfo(this.unsubscribe).pipe(
            filter((data: IChartLayoutInfoResponse) => {
                return data !== null && data !== undefined;
            }),
            tap((data: IChartLayoutInfoResponse) => {
                this.chartInfo[SOURCE_TYPE.AGENT_INSPECTOR] = data;
            }),
            map((data: IChartLayoutInfoResponse) => {
                return this.extractVisibleState(data[Object.keys(data)[0]], SOURCE_TYPE.AGENT_INSPECTOR);
            })
        );
    }
    getDefaultChartList(type: string): string[] {
        return this.getChartListOfType(type);
    }
    getChartLayoutInfo(type: string): Observable<IChartLayoutInfoResponse> {
        let data;
        if (type === SOURCE_TYPE.APPLICATION_INSPECTOR) {
            data = this.webAppSettingDataService.getApplicationLayoutInfo();
        } else {
            data = this.webAppSettingDataService.getAgentLayoutInfo();
        }
        const dataKey = Object.keys(data)[0];
        let newData: IChartLayoutInfoResponse = {};
        if (data[dataKey].length === 0) {
            newData[dataKey] = this.makeDefaultChartLayoutInfo(this.getChartListOfType(type));
        } else {
            newData = data;
        }
        return of(newData);
    }
    getChartVisibleState(type: string): Observable<{[key: string]: boolean}> {
        switch (type) {
            case SOURCE_TYPE.APPLICATION_INSPECTOR:
                return this.applicationChartVisibleState;
            case SOURCE_TYPE.AGENT_INSPECTOR:
                return this.agentChartVisibleState;
            default:
                return this.applicationChartVisibleState;
        }
    }
    setChartVisibleState(type: string, chartState: {[key: string]: boolean}): void {
        const targetChartData = this.chartInfo[type as SOURCE_TYPE];
        const key = Object.keys(targetChartData)[0];
        const chartInfoList = targetChartData[key];
        let lastIndex = -1;
        chartInfoList.forEach((chartInfo: IChartLayoutInfo) => {
            lastIndex = Math.max(chartInfo.index, lastIndex);
        });
        chartInfoList.forEach((chartInfo: IChartLayoutInfo) => {
            chartInfo.visible = chartState[chartInfo.chartName];
            if (chartInfo.visible === false) {
                chartInfo.index = -1;
            } else if (chartInfo.visible === true && chartInfo.index === -1) {
                chartInfo.index = lastIndex + 1;
                lastIndex++;
            }
        });
        this.updateChartLayoutInfo(type, {[key]: chartInfoList});
    }
    setChartOrderState(type: string, chartOrder: string[]): void {
        const targetChartData = this.chartInfo[type as SOURCE_TYPE];
        const key = Object.keys(targetChartData)[0];
        const chartInfoList = targetChartData[key];
        chartInfoList.forEach((chartInfo: IChartLayoutInfo) => {
            const index = chartOrder.findIndex((chartName: string) => {
                return chartInfo.chartName === chartName;
            });
            if (index === -1) {
                chartInfo.index = index;
                chartInfo.visible = false;
            } else {
                chartInfo.index = index;
            }
        });
        this.updateChartLayoutInfo(type, {[key]: chartInfoList});
    }
    private updateChartLayoutInfo(type: string, data: IChartLayoutInfoResponse): void {
        if (type === SOURCE_TYPE.APPLICATION_INSPECTOR) {
            this.webAppSettingDataService.setApplicationLayoutInfo(data);
        } else {
            this.webAppSettingDataService.setAgentLayoutInfo(data);
        }
        this.updateStore(type, data);
    }
    private updateStore(type: string, data: IChartLayoutInfoResponse): void {
        if (type === SOURCE_TYPE.APPLICATION_INSPECTOR) {
            this.storeHelperService.dispatch(new Actions.UpdateApplicationInspectorChartLayout(data));
        } else {
            this.storeHelperService.dispatch(new Actions.UpdateAgentInspectorChartLayout(data));
        }
    }
    private extractVisibleState(chartInfo: IChartLayoutInfo[], type: string): {[key: string]: boolean} {
        if (chartInfo.length === 0) {
            const defaultChartList = this.getChartListOfType(type);
            return defaultChartList.reduce((acc: any, chartName: string) => {
                acc[chartName] = true;
                return acc;
            }, {});
        } else {
            return chartInfo.reduce((acc: any, info: IChartLayoutInfo) => {
                acc[info.chartName] = info.visible;
                return acc;
            }, {});
        }
    }
    private makeDefaultChartLayoutInfo(chartList: string[]): IChartLayoutInfo[] {
        return chartList.map((chartName: string, index: number) => {
            return {
                chartName,
                index,
                'visible': true
            };
        });
    }
    private getChartListOfType(type: string): string[] {
        return type === SOURCE_TYPE.APPLICATION_INSPECTOR ?
            this.webAppSettingDataService.getApplicationInspectorDefaultChartList() :
            this.webAppSettingDataService.getAgentInspectorDefaultChartList();
    }
}
