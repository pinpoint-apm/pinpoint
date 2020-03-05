import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';

import { ServerMapData } from 'app/core/components/server-map/class';

@Component({
    selector: 'pp-server-map-context-popup',
    templateUrl: './server-map-context-popup.component.html',
    styleUrls: ['./server-map-context-popup.component.css']
})
export class ServerMapContextPopupComponent implements OnInit {
    @Input() data: ServerMapData;
    @Output() outChangeMergeState = new EventEmitter<IServerMapMergeState>();
    @Output() outClickRefresh = new EventEmitter<void>();

    mergeState: {[key: string]: boolean};

    constructor() {}
    ngOnInit() {
        this.mergeState = this.data.getMergeState();
    }

    onChangeMergeState({key, value}: {[key: string]: any}): void {
        this.outChangeMergeState.emit({
            name: key,
            state: !value
        });
    }

    onClickRefresh(): void {
        this.outClickRefresh.emit();
    }
}
