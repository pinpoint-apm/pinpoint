import { Component, OnInit, OnDestroy } from '@angular/core';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

import { ApplicationListDataService } from 'app/core/components/application-list/application-list-data.service';
import { AgentManagerDataService } from './agent-manager-data.service';

@Component({
    selector: 'pp-agent-manager-container',
    templateUrl: './agent-manager-container.component.html',
    styleUrls: ['./agent-manager-container.component.css']
})
export class AgentManagerContainerComponent implements OnInit, OnDestroy {
    private unsubscribe: Subject<void> = new Subject();
    applicationFilter = '';
    showLoading  = false;
    applicationList: IApplication[];
    agentList: {
        [key: string]: any;
    } = {};
    canRemoveInactiveAgent = false;
    constructor(
        private applicationListDataService: ApplicationListDataService,
        private agentManagerDataService: AgentManagerDataService
    ) {}
    ngOnInit() {
        this.applicationListDataService.getApplicationList().pipe(
            takeUntil(this.unsubscribe)
        ).subscribe((applicationList: IApplication[]) => {
            this.applicationList = applicationList;
        });
    }
    ngOnDestroy() {
        this.unsubscribe.next();
        this.unsubscribe.complete();
    }
    getAgentList(application: IApplication): string[] {
        return this.agentList[application.applicationName];
    }
    onLoadAgentList(applicationName: string): void {
        this.agentManagerDataService.getAgentList(applicationName).subscribe((agentList: any) => {
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
        });
    }
    onRemoveInactiveAgents(): void {
        if (this.canRemoveInactiveAgent === false) {
            return;
        }
        this.showLoading = true;
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
        console.log( this.canRemoveInactiveAgent );
    }
}
