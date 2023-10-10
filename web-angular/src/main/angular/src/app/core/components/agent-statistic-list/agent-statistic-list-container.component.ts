import {Component, OnInit, OnDestroy} from '@angular/core';
import {Subject, Observable} from 'rxjs';
import {map} from 'rxjs/operators';

import {UrlPath} from 'app/shared/models';
import {EndTime} from 'app/core/models/end-time';
import {StoreHelperService, UrlRouteManagerService, AnalyticsService, TRACKED_EVENT_LIST} from 'app/shared/services';
import {IGridData} from './agent-statistic-list.component';

@Component({
    selector: 'pp-agent-statistic-list-container',
    templateUrl: './agent-statistic-list-container.component.html',
    styleUrls: ['./agent-statistic-list-container.component.css']
})
export class AgentStatisticListContainerComponent implements OnInit, OnDestroy {
    private unsubscribe = new Subject<void>();

    agentListData$: Observable<IGridData[]>;

    constructor(
        private storeHelperService: StoreHelperService,
        private urlRouteManagerService: UrlRouteManagerService,
        private analyticsService: AnalyticsService,
    ) {
    }

    ngOnInit() {
        this.agentListData$ = this.storeHelperService.getAgentList(this.unsubscribe).pipe(
            map((data: IServerAndAgentDataV2[]) => this.makeGridData(data))
        );
    }

    ngOnDestroy() {
        this.unsubscribe.next();
        this.unsubscribe.complete();
    }

    private makeGridData(data: IServerAndAgentDataV2[]): IGridData[] {
        let index = 1;
        const resultData: IGridData[] = [];
        data.forEach(({instancesList}: IServerAndAgentDataV2) => {
            let row: IGridData;

            instancesList.forEach((agent: IAgentDataV2, agentIndex: number) => {
                if (agentIndex === 0) {
                    row = this.makeRow(agent, index, true, false);
                } else {
                    row.children.push(this.makeRow(agent, index, false, true));
                }
                index++;
            });
            resultData.push(row);
        });

        return resultData;
    }

    private makeRow(agent: IAgentDataV2, index: number, hasChild: boolean, isChild: boolean): any {
        const oRow: IGridData = {
            index: index,
            application: agent.applicationName,
            serviceType: agent.serviceType,
            agent: agent.agentId,
            agentName: agent.agentName ? agent.agentName : 'N/A',
            agentVersion: agent.agentVersion,
            startTimestamp: agent.startTimestamp,
            jvmVersion: agent.jvmInfo ? agent.jvmInfo.jvmVersion : ''
        };
        if (hasChild) {
            oRow.folder = true;
            oRow.open = true;
            oRow.children = [];
        }

        return oRow;
    }

    onCellClick(params: any): void {
        if (params.colDef.field === 'application') {
            this.urlRouteManagerService.openPage({
                path: [
                    UrlPath.MAIN,
                    `${params.data.application}@${params.data.serviceType}`
                ]
            });
            this.analyticsService.trackEvent(TRACKED_EVENT_LIST.CLICK_APPLICATION_IN_STATISTIC_LIST);
        } else {
            const diffMinute = 5;
            const startTime = EndTime.newByNumber(params.data.startTimestamp);
            this.urlRouteManagerService.openPage({
                path: [
                    UrlPath.INSPECTOR,
                    `${params.data.application}@${params.data.serviceType}`,
                    `${diffMinute}m`,
                    startTime.calcuNextTime(diffMinute).getEndTime(),
                    params.data.agent
                ]
            });
            this.analyticsService.trackEvent(TRACKED_EVENT_LIST.CLICK_AGENT_IN_STATISTIC_LIST);
        }

    }
}
