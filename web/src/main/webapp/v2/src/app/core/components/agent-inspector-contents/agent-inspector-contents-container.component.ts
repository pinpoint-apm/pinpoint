import { Component, OnInit, OnDestroy } from '@angular/core';
import { Subject, Observable, merge, of } from 'rxjs';
import { map } from 'rxjs/operators';

import { MessageQueueService, MESSAGE_TO, WebAppSettingDataService, NewUrlStateNotificationService } from 'app/shared/services';

@Component({
    selector: 'pp-agent-inspector-contents-container',
    templateUrl: './agent-inspector-contents-container.component.html',
    styleUrls: ['./agent-inspector-contents-container.component.css'],
})
export class AgentInspectorContentsContainerComponent implements OnInit, OnDestroy {
    private unsubscribe = new Subject<void>();

    gridLayout$: Observable<string>;
    coverRangeElements$: Observable<boolean>;

    constructor(
        private webAppSettingDataService: WebAppSettingDataService,
        private messageQueueService: MessageQueueService,
        private newUrlStateNotificationService: NewUrlStateNotificationService
    ) {}

    ngOnInit() {
        this.gridLayout$ = merge(
            of(this.webAppSettingDataService.getChartLayoutOption()),
            this.messageQueueService.receiveMessage(this.unsubscribe, MESSAGE_TO.INSPECTOR_CHART_SET_LAYOUT).pipe(
                map(([chartNumbPerRow]: number[]) => chartNumbPerRow)
            )
        ).pipe(
            map((chartNumPerRow: number) => this.getGridLayout(chartNumPerRow))
        );
        this.coverRangeElements$ = this.newUrlStateNotificationService.onUrlStateChange$.pipe(
            map((urlService: NewUrlStateNotificationService) => urlService.isRealTimeMode())
        );
    }

    ngOnDestroy() {
        this.unsubscribe.next();
        this.unsubscribe.complete();
    }

    private getGridLayout(chartNumPerRow: number): string {
        return `repeat(${chartNumPerRow}, 1fr)`;
    }
}
