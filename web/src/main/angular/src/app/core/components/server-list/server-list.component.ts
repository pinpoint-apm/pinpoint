import { Component, OnInit, Input, Output, EventEmitter} from '@angular/core';

@Component({
    selector: 'pp-server-list',
    templateUrl: './server-list.component.html',
    styleUrls: ['./server-list.component.css'],
})
export class ServerListComponent implements OnInit {
    @Input() serverList: IServerAndAgentDataV2[];
    @Input() agentData: any = {};
    @Input() isWas: boolean;
    @Input() selectedAgent: string;
    @Input() funcImagePath: Function;
    @Output() outSelectAgent = new EventEmitter<string>();
    @Output() outOpenInspector = new EventEmitter<string>();

    constructor() {}
    ngOnInit() {}
    getAgentLabel({agentId, agentName}: IAgentDataV2): string {
        return `${agentId} (${this.getAgentName({agentName} as IAgentDataV2)})`;
    }

    getAgentName({agentName}: IAgentDataV2): string {
        return agentName ? agentName : 'N/A';
    }

    hasError({agentId}: IAgentDataV2): boolean {
        return this.agentData[agentId] && this.agentData[agentId]['Error'] > 0;
    }

    getAlertImgPath(): string {
        return this.funcImagePath('icon-alert');
    }

    onSelectAgent({agentId}: IAgentDataV2) {
        if (this.selectedAgent === agentId) {
            return;
        }

        this.outSelectAgent.emit(agentId);
    }

    onOpenInspector({agentId}: IAgentDataV2): void {
        this.outOpenInspector.emit(agentId);
    }

    isSelectedAgent({agentId}: IAgentDataV2): boolean {
        return this.selectedAgent === agentId;
    }

    getLabelMaxWidth(btnWrapper: HTMLElement): string {
        return `calc(100% - ${btnWrapper.offsetWidth}px)`;
    }
}
