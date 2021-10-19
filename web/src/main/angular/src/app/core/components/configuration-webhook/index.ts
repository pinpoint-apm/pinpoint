import { NgModule } from '@angular/core';

import { SharedModule } from 'app/shared';
import { ApplicationListModule } from 'app/core/components/application-list';
import { WebhookListModule } from 'app/core/components/webhook-list';
import { ConfigurationWebhookContainerComponent } from './configuration-webhook-container.component';

@NgModule({
    declarations: [
        ConfigurationWebhookContainerComponent
    ],
    imports: [
        SharedModule,
        ApplicationListModule,
        WebhookListModule,
    ],
    exports: [
        ConfigurationWebhookContainerComponent
    ],
    providers: []
})
export class ConfigurationWebhookModule { }
