import { Component, OnInit, OnDestroy, ChangeDetectionStrategy, ChangeDetectorRef  } from '@angular/core';
import { Subject, Observable } from 'rxjs';
import { takeUntil, filter } from 'rxjs/operators';

import { UrlPathId } from 'app/shared/models';
import { NewUrlStateNotificationService, StoreHelperService } from 'app/shared/services';
import { ServerMapForFilteredMapDataService } from 'app/core/components/server-map/server-map-for-filtered-map-data.service';

@Component({
    selector: 'pp-data-load-indicator-for-filtered-map-container',
    templateUrl: './data-load-indicator-for-filtered-map-container.component.html',
    styleUrls: ['./data-load-indicator-for-filtered-map-container.component.css'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class DataLoadIndicatorForFilteredMapContainerComponent implements OnInit, OnDestroy {
    private unsubscribe: Subject<null> = new Subject();
    timezone$: Observable<string>;
    dateFormat$: Observable<string>;
    rangeValue: number[];
    selectedRangeValue: number[];
    constructor(
        private changeDetectorRef: ChangeDetectorRef,
        private storeHelperService: StoreHelperService,
        private newUrlStateNotificationService: NewUrlStateNotificationService,
        private serverMapForFilteredMapDataService: ServerMapForFilteredMapDataService
    ) {}
    ngOnInit() {
        this.connectStore();
        this.newUrlStateNotificationService.onUrlStateChange$.pipe(
            takeUntil(this.unsubscribe),
            filter((urlService: NewUrlStateNotificationService) => {
                return urlService.hasValue(UrlPathId.END_TIME, UrlPathId.PERIOD);
            }
        )).subscribe((urlService: NewUrlStateNotificationService) => {
            const endTime = urlService.getEndTimeToNumber();
            this.rangeValue = [urlService.getStartTimeToNumber(), endTime];
            this.selectedRangeValue = [endTime, endTime];
            this.changeDetectorRef.detectChanges();
        });
        this.serverMapForFilteredMapDataService.onServerMapData$.pipe(
            takeUntil(this.unsubscribe)
        ).subscribe((serverMapAndScatterData: any) => {
            this.selectedRangeValue = [
                serverMapAndScatterData['lastFetchedTimestamp'],
                serverMapAndScatterData['applicationMapData']['range']['to']
            ];
            this.changeDetectorRef.detectChanges();
        });
    }
    ngOnDestroy() {
        this.unsubscribe.next();
        this.unsubscribe.complete();
    }
    private connectStore(): void {
        this.timezone$ = this.storeHelperService.getTimezone(this.unsubscribe);
        this.dateFormat$ = this.storeHelperService.getDateFormat(this.unsubscribe, 7);
    }
}
