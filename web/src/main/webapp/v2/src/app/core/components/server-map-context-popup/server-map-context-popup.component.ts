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

    mergeNodeStateMap: {[key: string]: boolean};
    objectKeys = Object.keys;
    mergeStateStyleClass: {[key: string]: {[key: string]: boolean}};

    constructor() {}
    ngOnInit() {
        this.mergeNodeStateMap = this.data.getMergeState();
        this.mergeStateStyleClass = Object.keys(this.mergeNodeStateMap).reduce((acc: {[key: string]: {[key: string]: boolean}}, curr: string) => {
            const shouldChecked = this.mergeNodeStateMap[curr];

            return {
                ...acc,
                [curr]: this.getIconClass(shouldChecked)
            };
        }, {});
    }

    onClickMergeCheck(name: string): void {
        this.mergeNodeStateMap[name] = !this.mergeNodeStateMap[name];
        this.mergeStateStyleClass[name] = this.getIconClass(this.mergeNodeStateMap[name]);
        this.outClickMergeCheck.emit({
            name,
            state: this.mergeNodeStateMap[name]
        });
    }

    onClickRefresh(): void {
        this.outClickRefresh.emit();
    }

    private getIconClass(shouldChecked: boolean): {[key: string]: boolean} {
        return {
            'fas fa-check-square': shouldChecked,
            'far fa-square': !shouldChecked
        };
    }
}
