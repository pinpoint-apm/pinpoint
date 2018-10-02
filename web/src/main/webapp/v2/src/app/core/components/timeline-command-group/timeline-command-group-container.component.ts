import { Component, OnInit, OnDestroy, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import * as moment from 'moment-timezone';
import { Subject, Observable, combineLatest } from 'rxjs';

import { StoreHelperService } from 'app/shared/services';

@Component({
    selector: 'pp-timeline-command-group-container',
    templateUrl: './timeline-command-group-container.component.html',
    styleUrls: ['./timeline-command-group-container.component.css'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class TimelineCommandGroupContainerComponent implements OnInit, OnDestroy {
    private unsubscribe: Subject<void> = new Subject();
    pointingTime: string;
    pointingTime$: Observable<string>;
    constructor(
        private changeDetectorRef: ChangeDetectorRef,
        private storeHelperService: StoreHelperService
    ) {}
    ngOnInit() {
        this.connectStore();
    }
    ngOnDestroy() {
        this.unsubscribe.next();
        this.unsubscribe.complete();
    }
    private connectStore(): void {
        combineLatest(
            this.storeHelperService.getDateFormat(this.unsubscribe, 0),
            this.storeHelperService.getTimezone(this.unsubscribe),
            this.storeHelperService.getInspectorTimelineSelectedTime(this.unsubscribe)
        ).subscribe((data: [string, string, number]) => {
            const dateFormat = data[0];
            const timezone = data[1];
            this.pointingTime = moment(data[2]).tz(timezone).format(dateFormat);
            this.changeDetectorRef.detectChanges();
        });
    }
}
