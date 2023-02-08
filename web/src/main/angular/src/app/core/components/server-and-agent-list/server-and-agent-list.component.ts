import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { trigger, state, style, animate, transition } from '@angular/animations';

import { SortOption } from './server-and-agent-list-container.component';

@Component({
    selector: 'pp-server-and-agent-list',
    templateUrl: './server-and-agent-list.component.html',
    styleUrls: ['./server-and-agent-list.component.css'],
    animations: [
        trigger('collapseSpread', [
            state('collapsed', style({
                height: 0,
                overflow: 'hidden'
            })),
            state('spreaded', style({
                height: 'auto'
            })),
            transition('collapsed <=> spreaded', [
                animate('0.5s')
            ])
        ]),
        trigger('rightDown', [
            state('collapsed', style({
                transform: 'none'
            })),
            state('spreaded', style({
                transform: 'rotate(90deg)'
            })),
            transition('collapsed <=> spreaded', [
                animate('0.3s')
            ])
        ])
    ]
})
export class ServerAndAgentListComponent implements OnInit {
    @Input() funcImagePath: Function;
    @Input()
    set serverKeyList(serverKeyList: string[]) {
        this._serverKeyList = serverKeyList;
        if (serverKeyList.length !== 0) {
            this.isCollapsed = serverKeyList.reduce((acc: { [key: string]: boolean }, curr: string) => {
                return { ...acc, [curr]: false };
            }, {});
        }
    }

    get serverKeyList(): string[] {
        return this._serverKeyList;
    }

    @Input() serverList: IServerAndAgentDataV2[];
    @Input() agentId: string;
    @Input() selectedSortOptionKey: SortOption
    @Output() outSelectAgent = new EventEmitter<string>();

    private _serverKeyList: string[];
    isCollapsed: { [key: string]: boolean };

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

    onSelectAgent(agentName: string) {
        this.outSelectAgent.emit(agentName);
    }

    toggleMenu(serverName: string): void {
        this.isCollapsed[serverName] = !this.isCollapsed[serverName];
    }

    getCollapsedState(serverName: string): string {
        return this.isCollapsed[serverName] ? 'collapsed' : 'spreaded';
    }

    isActive(element: HTMLElement): boolean {
        return Array.from(element.nextElementSibling.querySelectorAll('.l-agent-name-wrapper')).some((el: HTMLElement) => {
            return el.classList.contains('active');
        });
    }

    getAgentLabel({agentId, agentName}: IAgentDataV2): string {
        switch (this.selectedSortOptionKey) {
            case SortOption.ID:
            case SortOption.RECENT:
            default:
                return agentName ? `${agentId} (${agentName})` : `${agentId} (N/A)`; 
            case SortOption.NAME:
                return agentName ? agentName : `N/A (${agentId})`;
        }
    }
}
