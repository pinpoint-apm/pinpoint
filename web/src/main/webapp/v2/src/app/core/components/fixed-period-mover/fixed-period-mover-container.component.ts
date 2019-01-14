import { Component, OnInit, OnDestroy, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { Subject, Observable } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

import {
    StoreHelperService,
    NewUrlStateNotificationService,
    UrlRouteManagerService,
    AnalyticsService,
    TRACKED_EVENT_LIST
} from 'app/shared/services';
import { UrlPathId } from 'app/shared/models';
import { Period, EndTime } from 'app/core/models';

@Component({
    selector: 'pp-fixed-period-mover-container',
    templateUrl: './fixed-period-mover-container.component.html',
    styleUrls: ['./fixed-period-mover-container.component.css'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class FixedPeriodMoverContainerComponent implements OnInit, OnDestroy {
    private unsubscribe: Subject<void> = new Subject();
    hiddenComponent = true;
    period: Period;
    endTime: EndTime;
    timezone$: Observable<string>;
    dateFormat$: Observable<string>;
    constructor(
        private changeDetectorRef: ChangeDetectorRef,
        private newUrlStateNotificationService: NewUrlStateNotificationService,
        private urlRouteManagerService: UrlRouteManagerService,
        private analyticsService: AnalyticsService,
        private storeHelperService: StoreHelperService
    ) {}
    ngOnInit() {
        this.newUrlStateNotificationService.onUrlStateChange$.pipe(
            takeUntil(this.unsubscribe)
        ).subscribe((urlService: NewUrlStateNotificationService) => {
            if (urlService.hasValue(UrlPathId.PERIOD, UrlPathId.END_TIME)) {
                this.period = urlService.getPathValue(UrlPathId.PERIOD);
                this.endTime = urlService.getPathValue(UrlPathId.END_TIME);
                this.hiddenComponent = false;
            } else {
                this.hiddenComponent = true;
            }
            this.changeDetectorRef.markForCheck();
        });
        this.connectStore();
    }
    private connectStore(): void {
        this.timezone$ = this.storeHelperService.getTimezone();
        this.dateFormat$ = this.storeHelperService.getDateFormat(this.unsubscribe, 1);
    }
    onMovePeriod(moveTime: string): void {
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.CLICK_FIXED_PERIOD_MOVE_BUTTON);
        this.urlRouteManagerService.moveOnPage({
            url: [
                this.newUrlStateNotificationService.getStartPath(),
                this.newUrlStateNotificationService.getPathValue(UrlPathId.APPLICATION).getUrlStr(),
                this.period.getValueWithTime(),
                moveTime
            ]
        });
    }
    ngOnDestroy() {
        this.unsubscribe.next();
        this.unsubscribe.complete();
    }
}
