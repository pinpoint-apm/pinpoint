import { NgModule } from '@angular/core';

import { SharedModule } from 'app/shared';
import { InboundOutboundRangeSelectorComponent } from './inbound-outbound-range-selector.component';
import { InboundOutboundRangeSelectorContainerComponent } from './inbound-outbound-range-selector-container.component';
import { InboundOutboundRangeSelectorForConfigurationContainerComponent } from './inbound-outbound-range-selector-for-configuration-container.component';

@NgModule({
    declarations: [
        InboundOutboundRangeSelectorComponent,
        InboundOutboundRangeSelectorContainerComponent,
        InboundOutboundRangeSelectorForConfigurationContainerComponent
    ],
    imports: [
        SharedModule
    ],
    exports: [
        InboundOutboundRangeSelectorContainerComponent,
        InboundOutboundRangeSelectorForConfigurationContainerComponent
    ],
    providers: []
})
export class InboundOutboundRangeSelectorModule { }
