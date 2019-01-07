import { Component, OnInit, ChangeDetectionStrategy } from '@angular/core';

import { AnalyticsService, TRACKED_EVENT_LIST, DynamicPopupService, StoreHelperService, WindowRefService } from 'app/shared/services';
import { ConfigurationPopupContainerComponent } from 'app/core/components/configuration-popup/configuration-popup-container.component';
import { Actions } from 'app/shared/store';

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
        private storeHelperService: StoreHelperService,
        private windowRefService: WindowRefService,
    ) {}

    ngOnInit() {}
    onOpenConfigurationPopup({coord}: {coord: ICoordinate}): void {
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.OPEN_CONFIGURATION_POPUP);
        this.dynamicPopupService.openPopup({
            coord,
            component: ConfigurationPopupContainerComponent
        });
        this.updateURLPathState();
    }

    private updateURLPathState(): void {
        const pathName = (this.windowRefService.nativeWindow as Window).location.pathname;

        this.storeHelperService.dispatch(new Actions.UpdateURLPath(pathName));
    }
}
