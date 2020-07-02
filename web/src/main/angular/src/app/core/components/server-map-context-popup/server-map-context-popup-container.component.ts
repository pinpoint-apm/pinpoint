import { Component, OnInit, Input, Output, EventEmitter, AfterViewInit } from '@angular/core';

import { ServerMapInteractionService } from 'app/core/components/server-map/server-map-interaction.service';
import { ServerMapData } from 'app/core/components/server-map/class';
import { AnalyticsService, DynamicPopup, TRACKED_EVENT_LIST, } from 'app/shared/services';

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
        private analyticsService: AnalyticsService,
    ) {}

    ngOnInit() {}
    ngAfterViewInit() {
        this.outCreated.emit(this.coord);
    }

    onInputChange({coord}: {coord: ICoordinate}): void {
        this.outCreated.emit(coord);
    }

    onChangeMergeState(mergeState: IServerMapMergeState): void {
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.CHANGE_SERVER_MAP_MERGE_STATE, `${mergeState.state}`);
        this.serverMapInteractionService.setMergeState(mergeState);
        this.outClose.emit();
    }

    onClickRefresh(): void {
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.REFRESH_SERVER_MAP);
        this.serverMapInteractionService.setRefresh();
        this.outClose.emit();
    }

    onClickOutside(): void {
        this.outClose.emit();
    }
}
