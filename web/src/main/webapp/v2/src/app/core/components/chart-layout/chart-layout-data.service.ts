import { Injectable } from '@angular/core';
import { Subject, Observable } from 'rxjs';
import { map, filter } from 'rxjs/operators';

import { WebAppSettingDataService, StoreHelperService } from 'app/shared/services';

export enum SOURCE_TYPE {
    APPLICATION_INSPECTOR = 'APPLICATION_INSPECTOR',
    AGENT_INSPECTOR = 'AGENT_INSPECTOR'
}

@Injectable()
export class ChartLayoutDataService {
    private unsubscribe: Subject<null> = new Subject();
    private applicationChartOrderList: Observable<string[]>;
    private agentChartOrderList: Observable<string[]>;
    constructor(
        private storeHelperService: StoreHelperService,
        private webAppSettingDataService: WebAppSettingDataService
    ) {
        this.applicationChartOrderList = this.storeHelperService.getApplicationInspectorChartLayoutInfo(this.unsubscribe).pipe(
            filter((data: IChartLayoutInfoResponse) => {
                return data !== null && data !== undefined;
            }),
            map((data: IChartLayoutInfoResponse) => {
                return this.extractOrderState(data[Object.keys(data)[0]]);
            })
        );
        this.agentChartOrderList = this.storeHelperService.getAgentInspectorChartLayoutInfo(this.unsubscribe).pipe(
            filter((data: IChartLayoutInfoResponse) => {
                return data !== null && data !== undefined;
            }),
            map((data: IChartLayoutInfoResponse) => {
                return this.extractOrderState(data[Object.keys(data)[0]]);
            })
        );
    }
    getDefaultChartList(type: string): string[] {
        return type === SOURCE_TYPE.APPLICATION_INSPECTOR ?
            this.webAppSettingDataService.getApplicationInspectorDefaultChartList() :
            this.webAppSettingDataService.getAgentInspectorDefaultChartList();
    }
    getChartOrderList(type: string): Observable<string[]> {
        switch (type) {
            case SOURCE_TYPE.APPLICATION_INSPECTOR:
                return this.applicationChartOrderList;
            case SOURCE_TYPE.AGENT_INSPECTOR:
                return this.agentChartOrderList;
            default:
                return this.applicationChartOrderList;
        }
    }
    private extractOrderState(chartInfo: IChartLayoutInfo[]): string[] {
        return chartInfo.filter((info: IChartLayoutInfo) => {
            return info.visible === true || info.index >= 0;
        }).sort((prev: IChartLayoutInfo, next: IChartLayoutInfo) => {
            return prev.index === -1 || next.index === -1 ? -1 : prev.index - next.index;
        }).map((info: IChartLayoutInfo) => {
            return info.chartName;
        });
    }
}
