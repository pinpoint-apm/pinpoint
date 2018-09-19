import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';

interface IAgentShortInfo {
    applicationName: string;
    agentId: string;
}

@Component({
    selector: 'pp-agent-manager',
    templateUrl: './agent-manager.component.html',
    styleUrls: ['./agent-manager.component.css']
})
export class AgentManagerComponent implements OnInit {
    @Input() application: IApplication;
    @Input()
    set agentList(value: IAgentShortInfo[]) {
        this._agentList = value;
        this.showLoading = false;
    }
    @Output() outLoadAgentList: EventEmitter<string> = new EventEmitter();
    @Output() outRemoveAgent: EventEmitter<string[]> = new EventEmitter();
    _agentList: IAgentShortInfo[];
    showLoading = false;
    constructor() {}
    ngOnInit() {}
    onLoadAgentList(): void {
        if (this.showLoading === true) {
            return;
        }
        this.showLoading = true;
        this.outLoadAgentList.emit(this.application.applicationName);
    }
    onRemoveAgent(agentId: string): void {
        this.outRemoveAgent.emit([this.application.applicationName, agentId]);
    }
    getAgentStateClass(): string {
        return 'l-' + this.getAgentState();
    }
    getAgentState(): string {
        if (this._agentList) {
            return this._agentList.length > 0 ? 'has' : 'empty';
        } else {
            return 'yet';
        }
    }
    isDup(agent: IAgentShortInfo): boolean {
        return this.application.applicationName !== agent.applicationName;
    }
}
