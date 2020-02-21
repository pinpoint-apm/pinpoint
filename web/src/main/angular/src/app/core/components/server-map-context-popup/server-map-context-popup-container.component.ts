import { Component, OnInit, Input, Output, EventEmitter, AfterViewInit } from '@angular/core';

import { ServerMapInteractionService } from 'app/core/components/server-map/server-map-interaction.service';
import { ServerMapData } from 'app/core/components/server-map/class';
import { DynamicPopup } from 'app/shared/services/dynamic-popup.service';

@Component({
    selector: 'pp-server-map-context-popup-container',
    templateUrl: './server-map-context-popup-container.component.html',
    styleUrls: ['./server-map-context-popup-container.component.css']
})
export class ServerMapContextPopupContainerComponent implements OnInit, AfterViewInit, DynamicPopup {
    @Input() data: ServerMapData;
    @Input() coord: ICoordinate;
    @Output() outCreated = new EventEmitter<ICoordinate>();
    @Output() outClose = new EventEmitter<void>();

    constructor(
        private serverMapInteractionService: ServerMapInteractionService,
    ) {}

    ngOnInit() {}
    ngAfterViewInit() {
        this.outCreated.emit(this.coord);
    }

    onInputChange({coord}: {coord: ICoordinate}): void {
        this.outCreated.emit(coord);
    }

    onChangeMergeState(mergeState: IServerMapMergeState): void {
        this.serverMapInteractionService.setMergeState(mergeState);
        this.outClose.emit();
    }

    onClickRefresh(): void {
        this.serverMapInteractionService.setRefresh();
        this.outClose.emit();
    }

    onClickOutside(): void {
        this.outClose.emit();
    }
}
