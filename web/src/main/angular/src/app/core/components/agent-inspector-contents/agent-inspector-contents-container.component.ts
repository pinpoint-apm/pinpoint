import { Component, OnInit, OnDestroy, HostBinding, Renderer2, ViewChild, ElementRef } from '@angular/core';
import { Subject, Observable } from 'rxjs';
import { map } from 'rxjs/operators';

import { NewUrlStateNotificationService } from 'app/shared/services';
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
    private chartNumPerRow = 3; // Set this 3 temporarily. Should be responsive later

    coverRangeElements$: Observable<boolean>;
    chartType = ChartType;

    constructor(
        private newUrlStateNotificationService: NewUrlStateNotificationService,
        private renderer: Renderer2
    ) {}

    ngOnInit() {
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
