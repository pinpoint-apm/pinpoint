import { Component, OnInit } from '@angular/core';
import { Observable } from 'rxjs';

import { DynamicPopupService } from 'app/shared/services';
import { ApplicationListDataService } from 'app/core/components/application-list/application-list-data.service';
import { AgentManagerDataService } from './agent-manager-data.service';
import { ServerErrorPopupContainerComponent } from 'app/core/components/server-error-popup';

@Component({
    selector: 'pp-agent-manager-container',
    templateUrl: './agent-manager-container.component.html',
    styleUrls: ['./agent-manager-container.component.css']
})
export class AgentManagerContainerComponent implements OnInit {
    applicationFilter = '';
    showLoading  = false;
    applicationList$: Observable<IApplication[]>;
    agentList: {
        [key: string]: any;
    } = {};
    canRemoveInactiveAgent = false;
    constructor(
        private applicationListDataService: ApplicationListDataService,
        private agentManagerDataService: AgentManagerDataService,
        private dynamicPopupService: DynamicPopupService
    ) {}
    ngOnInit() {
        this.applicationList$ = this.applicationListDataService.getApplicationList();
    }
    getAgentList(application: IApplication): string[] {
        return this.agentList[application.applicationName];
    }
    onLoadAgentList(applicationName: string): void {
        this.agentManagerDataService.getAgentList(applicationName).subscribe((agentList: IAgentList) => {
            const agentInfoList: any[] = [];
            Object.keys(agentList).forEach((key: string) => {
                agentList[key].forEach((agent: IAgent) => {
                    agentInfoList.push({
                        applicationName: agent.applicationName,
                        agentId: agent.agentId
                    });
                });
            });
            this.agentList[applicationName] = agentInfoList;
        }, (error: IServerErrorFormat) => {
            this.dynamicPopupService.openPopup({
                data: {
                    title: 'Error',
                    contents: error
                },
                component: ServerErrorPopupContainerComponent
            });
        });
    }
    onRemoveAgent([applicationName, agentId]: [string, string]): void {
        this.agentManagerDataService.removeAgentId(applicationName, agentId).subscribe((result: string) => {
            if (result === 'OK') {
                const appInfo = this.agentList[applicationName];
                const index = appInfo.findIndex((app: any) => {
                    return app.agentId === agentId;
                });
                appInfo.splice(index, 1);
            }
        }, (error: IServerErrorFormat) => {
            this.dynamicPopupService.openPopup({
                data: {
                    title: 'Error',
                    contents: error
                },
                component: ServerErrorPopupContainerComponent
            });
        });
    }
    onRemoveInactiveAgents(): void {
        if (this.canRemoveInactiveAgent === false) {
            return;
        }
        this.showLoading = true;
        this.agentManagerDataService.removeInactiveAgents().subscribe((result: string) => {
            if (result === 'OK') {
                // 모든 agent 목록 초기화
            }
        }, (error: IServerErrorFormat) => {
            this.dynamicPopupService.openPopup({
                data: {
                    title: 'Error',
                    contents: error
                },
                component: ServerErrorPopupContainerComponent
            });
        });
    }
    hasFilterStr(appName: string): boolean {
        const filter = this.applicationFilter.trim();
        if (filter === '') {
            return true;
        }
        if (appName.indexOf(filter) === -1) {
            return false;
        } else {
            return true;
        }
    }
    onChangeCanRemoveInactiveAgent($event: any): void {
        this.canRemoveInactiveAgent = $event.checked;
    }
}
