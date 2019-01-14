import { Component, OnInit, ChangeDetectionStrategy } from '@angular/core';

import { AnalyticsService, TRACKED_EVENT_LIST, DynamicPopupService } from 'app/shared/services';
import { ConfigurationPopupContainerComponent } from 'app/core/components/configuration-popup/configuration-popup-container.component';

@Component({
    selector: 'pp-command-group-container',
    templateUrl: './command-group-container.component.html',
    styleUrls: ['./command-group-container.component.css'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class CommandGroupContainerComponent implements OnInit {
    constructor(
        private dynamicPopupService: DynamicPopupService,
        private analyticsService: AnalyticsService,
    ) {}

    ngOnInit() {}
    onOpenConfigurationPopup(): void {
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.OPEN_CONFIGURATION_POPUP);
        this.dynamicPopupService.openPopup({
            component: ConfigurationPopupContainerComponent
        });
    }
}
