import { Component, OnInit, OnDestroy } from '@angular/core';
import * as moment from 'moment-timezone';
import { Subject, Observable, combineLatest } from 'rxjs';
import { map, filter } from 'rxjs/operators';

import { StoreHelperService } from 'app/shared/services';

@Component({
    selector: 'pp-timeline-command-group-container',
    templateUrl: './timeline-command-group-container.component.html',
    styleUrls: ['./timeline-command-group-container.component.css'],
})
export class TimelineCommandGroupContainerComponent implements OnInit, OnDestroy {
    private unsubscribe = new Subject<void>();

    pointingTime$: Observable<string>;

    constructor(
        private storeHelperService: StoreHelperService,
    ) {}

    ngOnInit() {
        this.connectStore();
    }

    ngOnDestroy() {
        this.unsubscribe.next();
        this.unsubscribe.complete();
    }

    private connectStore(): void {
        this.pointingTime$ = combineLatest(
            this.storeHelperService.getDateFormat(this.unsubscribe, 0),
            this.storeHelperService.getTimezone(this.unsubscribe),
            this.storeHelperService.getInspectorTimelineSelectedTime(this.unsubscribe).pipe(filter((time: number) => time !== 0))
        ).pipe(
            map(([dateFormat, timezone, pointingTime]: [string, string, number]) => {
                return moment(pointingTime).tz(timezone).format(dateFormat);
            })
        );
    }
}
