import { Component, OnInit, ComponentFactoryResolver, Injector, ChangeDetectionStrategy } from '@angular/core';

import { HELP_VIEWER_LIST, HelpViewerPopupContainerComponent } from 'app/core/components/help-viewer-popup/help-viewer-popup-container.component';
import { AnalyticsService, TRACKED_EVENT_LIST, DynamicPopupService } from 'app/shared/services';

@Component({
    selector: 'pp-filtered-map-contents-container',
    templateUrl: './filtered-map-contents-container.component.html',
    styleUrls: ['./filtered-map-contents-container.component.css'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class FilteredMapContentsContainerComponent implements OnInit {
    constructor(
        private analyticsService: AnalyticsService,
        private dynamicPopupService: DynamicPopupService,
        private componentFactoryResolver: ComponentFactoryResolver,
        private injector: Injector
    ) {}

    ngOnInit() {}
    onShowHelp($event: MouseEvent): void {
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.TOGGLE_HELP_VIEWER, HELP_VIEWER_LIST.SERVER_MAP);
        const {left, top, width, height} = ($event.target as HTMLElement).getBoundingClientRect();

        this.dynamicPopupService.openPopup({
            data: HELP_VIEWER_LIST.SERVER_MAP,
            coord: {
                coordX: left + width / 2,
                coordY: top + height / 2
            },
            component: HelpViewerPopupContainerComponent
        }, {
            resolver: this.componentFactoryResolver,
            injector: this.injector
        });
    }
}
