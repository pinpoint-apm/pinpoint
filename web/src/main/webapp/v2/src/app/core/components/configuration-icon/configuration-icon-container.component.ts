import { Component, OnInit, ChangeDetectionStrategy, Injector, ComponentFactoryResolver } from '@angular/core';

import { AnalyticsService, TRACKED_EVENT_LIST, DynamicPopupService } from 'app/shared/services';
import { ConfigurationPopupContainerComponent } from 'app/core/components/configuration-popup/configuration-popup-container.component';

@Component({
    selector: 'pp-configuration-icon-container',
    templateUrl: './configuration-icon-container.component.html',
    styleUrls: ['./configuration-icon-container.component.css'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class ConfigurationIconContainerComponent implements OnInit {
    constructor(
        private dynamicPopupService: DynamicPopupService,
        private analyticsService: AnalyticsService,
        private componentFactoryResolver: ComponentFactoryResolver,
        private injector: Injector
    ) {}

    ngOnInit() {}
    onOpenConfigurationPopup({coord}: {coord: ICoordinate}): void {
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.OPEN_CONFIGURATION_POPUP);
        this.dynamicPopupService.openPopup({
            coord,
            component: ConfigurationPopupContainerComponent
        }, {
            resolver: this.componentFactoryResolver,
            injector: this.injector
        });
    }
}
