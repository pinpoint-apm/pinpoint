import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';

@Component({
    selector: 'pp-server-map-context-popup',
    templateUrl: './server-map-context-popup.component.html',
    styleUrls: ['./server-map-context-popup.component.css']
})
export class ServerMapContextPopupComponent implements OnInit {
    @Input() mergeState: IServerMapMergeState;
    @Output() outChangeMergeState = new EventEmitter<IServerMapMergeState>();
    @Output() outClickRefresh = new EventEmitter<void>();

    constructor() {}
    ngOnInit() {}
    onChangeMergeState({key, value}: {[key: string]: any}): void {
        this.outChangeMergeState.emit({[key]: !value});
    }

    onClickRefresh(): void {
        this.outClickRefresh.emit();
    }
}
