import { NgModule } from '@angular/core';

import { SharedModule } from 'app/shared';
import { ApplicationListModule } from 'app/core/components/application-list';
import { AlarmRuleListModule } from 'app/core/components/alarm-rule-list';
import { ConfigurationAlarmContainerComponent } from './configuration-alarm-container.component';

@NgModule({
    declarations: [
        ConfigurationAlarmContainerComponent
    ],
    imports: [
        SharedModule,
        AlarmRuleListModule,
        ApplicationListModule,
    ],
    exports: [
        ConfigurationAlarmContainerComponent
    ],
    providers: []
})
export class ConfigurationAlarmModule { }
