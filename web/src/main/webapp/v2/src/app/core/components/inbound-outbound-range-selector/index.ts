
import { NgModule } from '@angular/core';
import { SharedModule } from 'app/shared';
import { InboundOutboundRangeSelectorComponent } from './inbound-outbound-range-selector.component';
import { InboundOutboundRangeSelectorContainerComponent } from './inbound-outbound-range-selector-container.component';
import { InboundOutboundRangeSelectorForConfigurationPopupContainerComponent } from './inbound-outbound-range-selector-for-configuration-popup-container.component';

@NgModule({
    declarations: [
        InboundOutboundRangeSelectorComponent,
        InboundOutboundRangeSelectorContainerComponent,
        InboundOutboundRangeSelectorForConfigurationPopupContainerComponent
    ],
    imports: [
        SharedModule
    ],
    exports: [
        InboundOutboundRangeSelectorContainerComponent,
        InboundOutboundRangeSelectorForConfigurationPopupContainerComponent
    ],
    providers: []
})
export class InboundOutboundRangeSelectorModule { }
