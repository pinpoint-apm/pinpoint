import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import * as md5 from 'blueimp-md5';

@Component({
    selector: 'pp-server-and-agent-list',
    templateUrl: './server-and-agent-list.component.html',
    styleUrls: ['./server-and-agent-list.component.css']
})
export class ServerAndAgentListComponent implements OnInit {
    @Input() funcImagePath: Function;
    @Input() serverKeyList: string[] = [];
    @Input() serverList: {[key: string]: IServerAndAgentData[]};
    @Input() agentId: string;
    @Output() outSelectAgent: EventEmitter<string> = new EventEmitter();

    constructor() {}
    ngOnInit() {}
    getIconPath(iconState: number) {
        let iconName = '';
        switch (iconState) {
            case 200:
            case 201:
                iconName = 'icon-down';
                break;
            case 300:
                iconName = 'icon-disconnect';
                break;
            case -1:
                iconName = 'icon-error';
                break;
            default:
                break;
        }
        return this.funcImagePath(iconName);
    }
    getAgentName(serverName: string, agentId: string): string {
        return serverName === 'Container' ? (md5(agentId) as string).substr(0, 5) : agentId;
    }
    onSelectAgent(agentName: string) {
        this.outSelectAgent.emit(agentName);
    }
}
