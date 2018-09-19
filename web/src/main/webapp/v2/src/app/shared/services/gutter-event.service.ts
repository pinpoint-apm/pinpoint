import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { filter } from 'rxjs/operators';

import { NewUrlStateNotificationService } from 'app/shared/services/new-url-state-notification.service';
import { SplitRatioService } from './split-ratio.service';

@Injectable()
export class GutterEventService {
    private outGutterResized: BehaviorSubject<number[]>;
    onGutterResized$: Observable<number[]>;

    constructor(
        private newUrlStateNotificationService: NewUrlStateNotificationService,
        private splitRatioService: SplitRatioService
    ) {
        this.newUrlStateNotificationService.onUrlStateChange$.pipe(
            filter((urlService: NewUrlStateNotificationService) => !!urlService),
        ).subscribe((urlService: NewUrlStateNotificationService) => {
            const view = urlService.getStartPath();

            this.outGutterResized = new BehaviorSubject(this.splitRatioService.getSplitRatio(view));
            this.onGutterResized$ = this.outGutterResized.asObservable();
        });
    }

    resizedGutter(sizes: number[]): void {
        this.outGutterResized.next(sizes);
    }
}
