import { Component, OnInit, OnDestroy } from '@angular/core';
import { Subject } from 'rxjs';
import { filter, takeUntil } from 'rxjs/operators';

import { UrlPathId } from 'app/shared/models';
import { NewUrlStateNotificationService } from 'app/shared/services';

@Component({
    selector: 'pp-thread-dump-page',
    templateUrl: './thread-dump-page.component.html',
    styleUrls: ['./thread-dump-page.component.css']
})
export class ThreadDumpPageComponent implements OnInit, OnDestroy {
    private unsubscribe: Subject<null> = new Subject();
    applicationName: string;
    agentId: string;

    constructor(
        private newUrlStateNotificationService: NewUrlStateNotificationService,
    ) {}

    ngOnInit() {
        this.newUrlStateNotificationService.onUrlStateChange$.pipe(
            takeUntil(this.unsubscribe),
            filter((urlService: NewUrlStateNotificationService) => {
                if (urlService.hasValue(UrlPathId.APPLICATION, UrlPathId.AGENT_ID)) {
                    return true;
                }
                return false;
            })
        ).subscribe((urlService: NewUrlStateNotificationService) => {
            this.applicationName = urlService.getPathValue(UrlPathId.APPLICATION).getApplicationName();
            this.agentId = urlService.getPathValue(UrlPathId.AGENT_ID);
        });
    }

    ngOnDestroy() {
        this.unsubscribe.next();
        this.unsubscribe.complete();
    }
}
