import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';

@Component({
    selector: 'pp-server-list',
    templateUrl: './server-list.component.html',
    styleUrls: ['./server-list.component.css'],
})
export class ServerListComponent implements OnInit {
    @Input() serverList: any = {};
    @Input() agentData: any = {};
    @Input() isWas: boolean;
    @Input() selectedAgent: string;
    @Input() funcImagePath: Function;
    @Output() outSelectAgent = new EventEmitter<string>();
    @Output() outOpenInspector = new EventEmitter<string>();

    constructor() {}
    ngOnInit() {}
    getServerKeys(): string[] {
        return Object.keys(this.serverList).sort();
    }

    getAgentKeys(serverName: string): string[] {
        return Object.keys(this.serverList[serverName]['instanceList']).sort();
    }

    getAgentName(serverName: string, agentId: string): string {
        const agentInfo = this.serverList[serverName]['instanceList'][agentId];
        return agentInfo && agentInfo['agentName'] ? agentInfo['agentName'] : null;
    }

    hasError(agent: string): boolean {
        return this.agentData[agent] && this.agentData[agent]['Error'] > 0;
    }

    getAlertImgPath(): string {
        return this.funcImagePath('icon-alert');
    }

    onSelectAgent(agent: string) {
        if (this.selectedAgent !== agent) {
            this.outSelectAgent.emit(agent);
        }
    }

    onOpenInspector(agent: string): void {
        this.outOpenInspector.emit(agent);
    }

    isSelectedAgent(agent: string): boolean {
        return this.selectedAgent === agent;
    }
}
