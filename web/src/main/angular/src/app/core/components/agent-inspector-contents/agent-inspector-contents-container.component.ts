import { Component, OnInit, OnDestroy, HostBinding, Renderer2, ViewChild, ElementRef } from '@angular/core';
import { Subject, Observable, merge, of } from 'rxjs';
import { map } from 'rxjs/operators';
import { TranslateService } from '@ngx-translate/core';

import { MessageQueueService, MESSAGE_TO, WebAppSettingDataService, NewUrlStateNotificationService } from 'app/shared/services';
import { ChartType } from 'app/core/components/inspector-chart/inspector-chart-container-factory';

@Component({
    selector: 'pp-agent-inspector-contents-container',
    templateUrl: './agent-inspector-contents-container.component.html',
    styleUrls: ['./agent-inspector-contents-container.component.css'],
})
export class AgentInspectorContentsContainerComponent implements OnInit, OnDestroy {
    @HostBinding('class') hostClass = 'l-agent-inspector-contents';
    @ViewChild('chartGroupWrapper', { static: true }) chartGroupWrapper: ElementRef;
    private unsubscribe = new Subject<void>();

    guideMessage$: Observable<string>;
    coverRangeElements$: Observable<boolean>;
    chartType = ChartType;

    constructor(
        private webAppSettingDataService: WebAppSettingDataService,
        private messageQueueService: MessageQueueService,
        private newUrlStateNotificationService: NewUrlStateNotificationService,
        private translateService: TranslateService,
        private renderer: Renderer2
    ) {}

    ngOnInit() {
        this.coverRangeElements$ = this.newUrlStateNotificationService.onUrlStateChange$.pipe(
            map((urlService: NewUrlStateNotificationService) => urlService.isRealTimeMode())
        );
        this.guideMessage$ = this.translateService.get('INSPECTOR.CHART_INTERACTION_GUIDE_MESSAGE');
        merge(
            of(this.webAppSettingDataService.getChartLayoutOption()),
            this.messageQueueService.receiveMessage(this.unsubscribe, MESSAGE_TO.INSPECTOR_CHART_SET_LAYOUT)
        ).subscribe((chartCountPerRow: number) => {
            this.renderer.setStyle(this.chartGroupWrapper.nativeElement, 'grid-template-columns', this.getGridLayout(chartCountPerRow));
        });
    }

    ngOnDestroy() {
        this.unsubscribe.next();
        this.unsubscribe.complete();
    }

    private getGridLayout(chartNumPerRow: number): string {
        return `repeat(${chartNumPerRow}, 1fr)`;
    }
}
