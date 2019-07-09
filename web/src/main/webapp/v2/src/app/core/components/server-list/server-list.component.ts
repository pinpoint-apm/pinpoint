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
    @Input() funcImagePath: Function;
    @Output() outSelectAgent = new EventEmitter<string>();
    @Output() outOpenInspector = new EventEmitter<string>();

    private selectedAgent: string;

    constructor() {}
    ngOnInit() {
        this.selectedAgent = this.getAgentKeys(this.getServerKeys()[0])[0];
    }

    getServerKeys(): string[] {
        return Object.keys(this.serverList).sort();
    }

    getAgentKeys(serverName: string): string[] {
        return Object.keys(this.serverList[serverName]['instanceList']).sort();
    }

    hasError(agentName: string): boolean {
        return this.agentData[agentName] && this.agentData[agentName]['Error'] > 0;
    }

    getAlertImgPath(): string {
        return this.funcImagePath('icon-alert');
    }

    onSelectAgent(agent: string) {
        if (this.selectedAgent !== agent) {
            this.selectedAgent = agent;
            this.outSelectAgent.emit(agent);
        }
    }

    onOpenInspector(agentName: string): void {
        this.outOpenInspector.emit(agentName);
    }
}
