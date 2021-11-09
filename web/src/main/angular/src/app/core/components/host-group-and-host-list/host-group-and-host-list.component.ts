import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { trigger, state, style, animate, transition } from '@angular/animations';

@Component({
    selector: 'pp-host-group-and-host-list',
    templateUrl: './host-group-and-host-list.component.html',
    styleUrls: ['./host-group-and-host-list.component.css'],
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
export class HostGroupAndHostListComponent implements OnInit {
    @Input() hostList: string[];
    @Input() selectedHostGroup: string;
    @Input() selectedHost: string;
    @Output() outSelectHost = new EventEmitter<string>();

    isCollapsed = false;

    constructor() {}
    ngOnInit() {}
    onSelectHost(host: string) {
        this.outSelectHost.emit(host);
    }

    toggleMenu(): void {
        this.isCollapsed = !this.isCollapsed;
    }

    getCollapsedState(): string {
        return this.isCollapsed ? 'collapsed' : 'spreaded';
    }
}
