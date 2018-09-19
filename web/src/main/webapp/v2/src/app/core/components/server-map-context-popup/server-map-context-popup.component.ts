import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';

import { ServerMapData } from 'app/core/components/server-map/class';

@Component({
    selector: 'pp-server-map-context-popup',
    templateUrl: './server-map-context-popup.component.html',
    styleUrls: ['./server-map-context-popup.component.css']
})
export class ServerMapContextPopupComponent implements OnInit {
    @Input() data: ServerMapData;
    @Output() outClickMergeCheck = new EventEmitter<IServerMapMergeState>();
    @Output() outClickRefresh = new EventEmitter<void>();

    mergeNodeStateMap: { [key: string]: boolean };
    objectKeys = Object.keys;

    constructor() {}
    ngOnInit() {
        this.mergeNodeStateMap = this.data.getMergeState();
    }

    onClickMergeCheck(name: string): void {
        this.mergeNodeStateMap[name] = !this.mergeNodeStateMap[name];
        this.outClickMergeCheck.emit({
            name,
            state: this.mergeNodeStateMap[name]
        });
    }
    onClickRefresh(): void {
        this.outClickRefresh.emit();
    }
    getClassState(key: string): any {
        return this.mergeNodeStateMap[key] ? {
            fas: true,
            'fa-check-square': true
        } : {
            far: true,
            'fa-square': true
        };
    }
}
