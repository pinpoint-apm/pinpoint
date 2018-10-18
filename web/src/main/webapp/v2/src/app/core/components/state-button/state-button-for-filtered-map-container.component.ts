import { Component, OnInit, OnDestroy, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { Subject } from 'rxjs';

import { StoreHelperService } from 'app/shared/services';
import { ServerMapForFilteredMapDataService } from 'app/core/components/server-map/server-map-for-filtered-map-data.service';
import { BUTTON_STATE } from './state-button.component';

@Component({
    selector: 'pp-state-button-for-filtered-map-container',
    templateUrl: './state-button-for-filtered-map-container.component.html',
    styleUrls: ['./state-button-for-filtered-map-container.component.css'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class StateButtonForFilteredMapContainerComponent implements OnInit, OnDestroy {
    private unsubscribe: Subject<null> = new Subject();
    showCountInfo = false;
    currentState = BUTTON_STATE.PAUSE;
    constructor(
        private changeDetectorRef: ChangeDetectorRef,
        private storeHelperService: StoreHelperService,
        private serverMapForFilteredMapDataService: ServerMapForFilteredMapDataService
    ) {}
    ngOnInit() {
        this.connectStore();
    }
    ngOnDestroy() {
        this.unsubscribe.next();
        this.unsubscribe.complete();
    }
    private connectStore(): void {
        this.storeHelperService.getServerMapLoadingState(this.unsubscribe).subscribe((state: string) => {
            switch (state) {
                case 'loading':
                    this.currentState = BUTTON_STATE.PAUSE;
                    break;
                case 'pause':
                    this.currentState = BUTTON_STATE.RESUME;
                    break;
                case 'completed':
                    this.currentState = BUTTON_STATE.COMPLETED;
                    break;
            }
            this.changeDetectorRef.detectChanges();
        });
    }
    onChangeState(event: string) {
        if ( event === BUTTON_STATE.RESUME ) {
            this.serverMapForFilteredMapDataService.resumeDataLoad();
        } else if ( event === BUTTON_STATE.PAUSE ) {
            this.serverMapForFilteredMapDataService.stopDataLoad();
        }
    }
}
