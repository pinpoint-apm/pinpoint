import { Component, OnInit, OnDestroy } from '@angular/core';
import { Subject } from 'rxjs';

import { UrlPath } from 'app/shared/models';
import { StoreHelperService, UrlRouteManagerService } from 'app/shared/services';

interface IGridData {
    index: number;
    application: string;
    serviceType: string;
    agent: string;
    agentVersion: string;
    jvmVersion: string;
    folder?: boolean;
    open?: boolean;
    children?: IGridData[];
}

@Component({
    selector: 'pp-agent-list-container',
    templateUrl: './agent-list-container.component.html',
    styleUrls: ['./agent-list-container.component.css']
})
export class AgentListContainerComponent implements OnInit, OnDestroy {
    private unsubscribe: Subject<void> = new Subject();
    agentCount = 0;
    agentListData: any;
    constructor(
        private storeHelperService: StoreHelperService,
        private urlRouteManagerService: UrlRouteManagerService
    ) {}
    ngOnInit() {
        this.connectStore();
    }
    ngOnDestroy() {
        this.unsubscribe.next();
        this.unsubscribe.complete();
    }
    private connectStore(): void {
        this.storeHelperService.getAgentList<IAgentList>(this.unsubscribe).subscribe((agentList: IAgentList) => {
            this.makeGridData(agentList);
        });
    }
    private makeGridData(agentList: IAgentList): void {
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
        this.agentCount = index - 1;
        this.agentListData = resultData;
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
        this.urlRouteManagerService.openPage([
            UrlPath.MAIN,
            params.application + '@' + params.serviceType
        ]);
    }
}
