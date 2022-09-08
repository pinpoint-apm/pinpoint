import { Component, OnInit, OnDestroy, HostBinding, ViewChild, ElementRef, Renderer2 } from '@angular/core';
import { Observable, of, Subject } from 'rxjs';
import { map } from 'rxjs/operators';

import { NewUrlStateNotificationService, WebAppSettingDataService } from 'app/shared/services';
import { ChartType } from 'app/core/components/inspector-chart/inspector-chart-container-factory';

@Component({
    selector: 'pp-application-inspector-contents-container',
    templateUrl: './application-inspector-contents-container.component.html',
    styleUrls: ['./application-inspector-contents-container.component.css']
})
export class ApplicationInspectorContentsContainerComponent implements OnInit, OnDestroy {
    @HostBinding('class') hostClass = 'l-application-inspector-contents';
    @ViewChild('chartGroupWrapper', { static: true }) chartGroupWrapper: ElementRef;
    private unsubscribe = new Subject<void>();
    private chartNumPerRow = 3; // Set this 3 temporarily. Should be responsive later

    isApplicationInspectorActivated$: Observable<boolean>;
    coverRangeElements$: Observable<boolean>;
    chartType = ChartType;

    constructor(
        private webAppSettingDataService: WebAppSettingDataService,
        private newUrlStateNotificationService: NewUrlStateNotificationService,
        private renderer: Renderer2
    ) {}

    ngOnInit() {
        this.isApplicationInspectorActivated$ = this.webAppSettingDataService.isApplicationInspectorActivated();
        this.coverRangeElements$ = this.newUrlStateNotificationService.onUrlStateChange$.pipe(
            map((urlService: NewUrlStateNotificationService) => urlService.isRealTimeMode())
        );
        this.renderer.setStyle(this.chartGroupWrapper.nativeElement, 'grid-template-columns', this.getGridLayout(this.chartNumPerRow));

    }

    ngOnDestroy() {
        this.unsubscribe.next();
        this.unsubscribe.complete();
    }

    private getGridLayout(chartNumPerRow: number): string {
        return `repeat(${chartNumPerRow}, 1fr)`;
    }
}
