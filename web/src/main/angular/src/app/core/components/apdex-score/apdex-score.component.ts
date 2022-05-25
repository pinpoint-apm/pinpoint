import { Component, ComponentFactoryResolver, Injector, Input, OnChanges, OnInit, SimpleChanges } from '@angular/core';
import { AnalyticsService, DynamicPopupService, TRACKED_EVENT_LIST } from 'app/shared/services';

import { HELP_VIEWER_LIST, HelpViewerPopupContainerComponent } from 'app/core/components/help-viewer-popup/help-viewer-popup-container.component';

const enum APDEX_SCORE_RANK {
    EXCELLENT = 'excellent',
    GOOD = 'good',
    FAIR = 'fair',
    POOR = 'poor',
    UNACCEPTABLE = 'unacceptable',
}

@Component({
    selector: 'pp-apdex-score',
    templateUrl: './apdex-score.component.html',
    styleUrls: ['./apdex-score.component.css']
})
export class ApdexScoreComponent implements OnInit, OnChanges {
    @Input() score: number;

    fixedScore: string;

    constructor(
        private injector: Injector,
        private analyticsService: AnalyticsService,
        private dynamicPopupService: DynamicPopupService,
        private componentFactoryResolver: ComponentFactoryResolver,
    ) { }

    ngOnInit() {
    }

    ngOnChanges(changes: SimpleChanges) {
        const score: number = changes.score.currentValue;

        if (!isNaN(Number(score))) {
            this.fixedScore = (Math.floor(score * 100) / 100).toFixed(2);
        }
    }

    getScoreTextClassName() {
        let className = '';

        if (!this.score) {
            return 'score';
        }

        if (this.score < 0.5) {
            className = APDEX_SCORE_RANK.UNACCEPTABLE;
        } else if (this.score < 0.7) {
            className = APDEX_SCORE_RANK.POOR;
        } else if (this.score < 0.85) {
            className = APDEX_SCORE_RANK.FAIR;
        } else if (this.score < 0.94) {
            className = APDEX_SCORE_RANK.GOOD;
        } else {
            className = APDEX_SCORE_RANK.EXCELLENT;
        }

        return `score ${className}`;
    }

    onShowHelp($event: MouseEvent): void {
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.CLICK_APDEX_SCORE, HELP_VIEWER_LIST.APDEX_SCORE);
        const { left, top, height } = ($event.target as HTMLElement).getBoundingClientRect();

        this.dynamicPopupService.openPopup({
            data: HELP_VIEWER_LIST.APDEX_SCORE,
            coord: {
                coordX: left,
                coordY: top + height / 2
            },
            component: HelpViewerPopupContainerComponent
        }, {
            resolver: this.componentFactoryResolver,
            injector: this.injector
        });
    }
}
