import { Component, OnInit, OnDestroy } from '@angular/core';
import { Subject, Observable } from 'rxjs';
import { map } from 'rxjs/operators';

import { UrlPath } from 'app/shared/models';
import { StoreHelperService, UrlRouteManagerService, AnalyticsService, TRACKED_EVENT_LIST } from 'app/shared/services';
import { IGridData } from './agent-statistic-list.component';

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
    ) {}

    ngOnInit() {
        this.agentListData$ = this.storeHelperService.getAgentList(this.unsubscribe).pipe(
            map((data: IAgentList) => this.makeGridData(data))
        );
    }

    ngOnDestroy() {
        this.unsubscribe.next();
        this.unsubscribe.complete();
    }

    private makeGridData(agentList: IAgentList): IGridData[] {
        let index = 1;
        const resultData: IGridData[] = [];
        Object.keys(agentList).forEach((key: string, innerIndex: number) => {
            const list: IAgent[] = agentList[key];
            let row: IGridData;

            if (list.length === 0) {
                row = this.makeRow(list[0], index, false, false);
                index++;
            } else {
                list.forEach((agent: IAgent, agentIndex: number) => {
                    if (agentIndex === 0) {
                        row = this.makeRow(agent, index, true, false);
                    } else {
                        row.children.push(this.makeRow(agent, index, false, true));
                    }
                    index++;
                });
            }
            resultData.push(row);
        });

        return resultData;
    }

    private makeRow(agent: IAgent, index: number, hasChild: boolean, isChild: boolean): any {
        const oRow: IGridData = {
            index: index,
            application: isChild ? '' : agent.applicationName,
            serviceType: agent.serviceType,
            agent: agent.agentId,
            agentVersion: agent.agentVersion,
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
        this.urlRouteManagerService.openPage({
            path: [
                UrlPath.MAIN,
                `${params.application}@${params.serviceType}`
            ]
        });
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.CLICK_APPLICATION_IN_STATISTIC_LIST);
    }
}
