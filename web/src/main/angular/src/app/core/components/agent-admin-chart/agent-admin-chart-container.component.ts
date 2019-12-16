import { Component, OnInit, OnDestroy } from '@angular/core';
import { Subject } from 'rxjs';
import { StoreHelperService } from 'app/shared/services';

@Component({
    selector: 'pp-agent-admin-chart-container',
    templateUrl: './agent-admin-chart-container.component.html',
    styleUrls: ['./agent-admin-chart-container.component.css']
})
export class AgentAdminChartContainerComponent implements OnInit, OnDestroy {
    private unsubscribe: Subject<void> = new Subject();
    showLoading = true;
    agentCount = 0;
    chartData: {
        jvmVersion: {
            [key: string]: number
        },
        agentVersion: {
            [key: string]: number
        }
    } = {
        jvmVersion: {},
        agentVersion: {}
    };
    constructor(
        private storeHelperService: StoreHelperService
    ) {}
    ngOnInit() {
        this.storeHelperService.getAgentList<IAgentList>(this.unsubscribe).subscribe((agentList: IAgentList) => {
            this.extractChartData(agentList);
            this.showLoading = false;
        });
    }
    ngOnDestroy() {
        this.unsubscribe.next();
        this.unsubscribe.complete();
    }
    private extractChartData(agentList: IAgentList): void {
        this.chartData = {
            jvmVersion: {},
            agentVersion: {}
        };
        let count = 0;
        Object.keys(agentList).forEach((key: string) => {
            const agents = agentList[key];
            agents.forEach((agent: IAgent) => {
                count++;
                if (agent.agentVersion) {
                    this.chartData.agentVersion[agent.agentVersion] = (this.chartData.agentVersion[agent.agentVersion] || 0) + 1;
                }
                if (agent.jvmInfo && agent.jvmInfo.jvmVersion) {
                    this.chartData.jvmVersion[agent.jvmInfo.jvmVersion] = (this.chartData.jvmVersion[agent.jvmInfo.jvmVersion] || 0) + 1;
                } else {
                    this.chartData.jvmVersion['UNKNOWN'] = (this.chartData.jvmVersion['UNKNOWN'] || 0) + 1;
                }
            });
        });
        this.agentCount = count;
    }
}
