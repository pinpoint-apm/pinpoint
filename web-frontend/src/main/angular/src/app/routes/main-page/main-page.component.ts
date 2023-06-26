import { Component, OnInit, ComponentFactoryResolver, Injector, ChangeDetectionStrategy, OnDestroy } from '@angular/core';
import { Observable, Subject } from 'rxjs';

import {
    WebAppSettingDataService,
    AnalyticsService,
    TRACKED_EVENT_LIST,
    DynamicPopupService,
} from 'app/shared/services';
import { HELP_VIEWER_LIST, HelpViewerPopupContainerComponent } from 'app/core/components/help-viewer-popup/help-viewer-popup-container.component';

@Component({
    selector: 'pp-main-page',
    templateUrl: './main-page.component.html',
    styleUrls: ['./main-page.component.css'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class MainPageComponent implements OnInit, OnDestroy {
    private unsubscribe = new Subject<void>();

    isAppSelected$: Observable<boolean>;

    constructor(
        private webAppSettingDataService: WebAppSettingDataService,
        private analyticsService: AnalyticsService,
        private dynamicPopupService: DynamicPopupService,
        private componentFactoryResolver: ComponentFactoryResolver,
        private injector: Injector
    ) {}

    ngOnInit() {
        this.webAppSettingDataService.getVersion().subscribe((version: string) => {
            this.analyticsService.trackEvent(TRACKED_EVENT_LIST.VERSION, version);
        });
    }

    ngOnDestroy() {
        this.unsubscribe.next();
        this.unsubscribe.complete();
    }

    // TODO: Put this in app-widget in new layout
    onShowHelp($event: MouseEvent): void {
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.TOGGLE_HELP_VIEWER, HELP_VIEWER_LIST.NAVBAR);
        const {left, top, width, height} = ($event.target as HTMLElement).getBoundingClientRect();

        this.dynamicPopupService.openPopup({
            data: HELP_VIEWER_LIST.NAVBAR,
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
