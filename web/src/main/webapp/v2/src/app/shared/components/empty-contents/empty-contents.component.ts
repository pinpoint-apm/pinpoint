import { Component, OnInit } from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';

import { UrlPathId } from 'app/shared/models';
import { NewUrlStateNotificationService } from 'app/shared/services';

@Component({
    selector: 'pp-empty-contents',
    templateUrl: './empty-contents.component.html',
    styleUrls: ['./empty-contents.component.css']
})
export class EmptyContentsComponent implements OnInit {
    hiddenComponent$: Observable<boolean>;
    constructor(
        private newUrlStateNotificationService: NewUrlStateNotificationService
    ) {}
    ngOnInit() {
        this.hiddenComponent$ = this.newUrlStateNotificationService.onUrlStateChange$.pipe(
            map((urlService: NewUrlStateNotificationService) => {
                return !urlService.hasValue(UrlPathId.PERIOD, UrlPathId.END_TIME);
            })
        );
    }
}
