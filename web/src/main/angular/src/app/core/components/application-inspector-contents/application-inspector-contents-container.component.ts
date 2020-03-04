import { Component, OnInit, OnDestroy, HostBinding, ViewChild, ElementRef, Renderer2 } from '@angular/core';
import { Observable, of, Subject, merge } from 'rxjs';
import { map } from 'rxjs/operators';

import { WebAppSettingDataService, MessageQueueService, MESSAGE_TO, NewUrlStateNotificationService } from 'app/shared/services';
import { ChartType } from 'app/core/components/inspector-chart/inspector-chart-container-factory';
import { TranslateService } from '@ngx-translate/core';

@Component({
    selector: 'pp-application-inspector-contents-container',
    templateUrl: './application-inspector-contents-container.component.html',
    styleUrls: ['./application-inspector-contents-container.component.css']
})
export class ApplicationInspectorContentsContainerComponent implements OnInit, OnDestroy {
    @HostBinding('class') hostClass = 'l-application-inspector-contents';
    @ViewChild('chartGroupWrapper', { static: true }) chartGroupWrapper: ElementRef;
    private unsubscribe = new Subject<void>();

    isApplicationInspectorActivated$: Observable<boolean>;
    isApplicationInspectorActivated: boolean;
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
        this.isApplicationInspectorActivated$ = this.webAppSettingDataService.isApplicationInspectorActivated();
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
