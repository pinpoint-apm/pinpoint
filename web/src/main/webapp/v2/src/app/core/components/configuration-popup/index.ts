
import { NgModule } from '@angular/core';
import { ClipboardModule } from 'ngx-clipboard';

import { SharedModule } from 'app/shared';
import { AlarmRuleListModule } from 'app/core/components/alarm-rule-list';
import { ApplicationListModule } from 'app/core/components/application-list';
import { UserGroupModule } from 'app/core/components/user-group';
import { GroupMemberModule } from 'app/core/components/group-member';
import { PinpointUserModule } from 'app/core/components/pinpoint-user';
import { InboundOutboundRangeSelectorModule } from 'app/core/components/inbound-outbound-range-selector';
import { SearchPeriodModule } from 'app/core/components/search-period';
import { TimezoneModule } from 'app/core/components/timezone/index';
import { DateFormatModule } from 'app/core/components/date-format';
import { DuplicationCheckModule } from 'app/core/components/duplication-check';
import { ConfigurationPopupComponent } from './configuration-popup.component';
import { ConfigurationPopupContainerComponent } from './configuration-popup-container.component';
import { ConfigurationPopupGeneralContainerComponent } from './configuration-popup-general-container.component';
import { ConfigurationPopupUsergroupComponent } from './configuration-popup-usergroup.component';
import { ConfigurationPopupAlarmComponent } from './configuration-popup-alarm.component';
import { ConfigurationPopupHelpContainerComponent } from './configuration-popup-help-container.component';
import { ConfigurationPopupInstallationContainerComponent } from './configuration-popup-installation-container.component';
import { ConfigurationPopupInstallationDownloadLinkComponent } from './configuration-popup-installation-download-link.component';
import { ConfigurationPopupInstallationJVMArgumentInfoComponent } from './configuration-popup-installation-jvm-argument-info.component';

import { ConfigurationPopupInstallationDataService } from './configuration-popup-installation-data.service';

@NgModule({
    declarations: [
        ConfigurationPopupComponent,
        ConfigurationPopupContainerComponent,
        ConfigurationPopupGeneralContainerComponent,
        ConfigurationPopupUsergroupComponent,
        ConfigurationPopupAlarmComponent,
        ConfigurationPopupHelpContainerComponent,
        ConfigurationPopupInstallationContainerComponent,
        ConfigurationPopupInstallationDownloadLinkComponent,
        ConfigurationPopupInstallationJVMArgumentInfoComponent
    ],
    imports: [
        SharedModule,
        ClipboardModule,
        AlarmRuleListModule,
        ApplicationListModule,
        UserGroupModule,
        GroupMemberModule,
        PinpointUserModule,
        InboundOutboundRangeSelectorModule,
        SearchPeriodModule,
        ApplicationListModule,
        TimezoneModule,
        DateFormatModule,
        DuplicationCheckModule,
    ],
    exports: [],
    entryComponents: [
        ConfigurationPopupContainerComponent,
        ConfigurationPopupGeneralContainerComponent,
        ConfigurationPopupUsergroupComponent,
        ConfigurationPopupAlarmComponent,
        ConfigurationPopupInstallationContainerComponent,
        ConfigurationPopupHelpContainerComponent
    ],
    providers: [
        ConfigurationPopupInstallationDataService
    ]
})
export class ConfigurationPopupModule { }
