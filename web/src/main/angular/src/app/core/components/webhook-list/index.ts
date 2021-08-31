import { NgModule } from '@angular/core';
import { SharedModule } from 'app/shared';
import { WebhookListContainerComponent } from './webhook-list-container.component';
import { WebhookListDataService } from './webhook-list-data.service';

@NgModule({
    declarations: [
        WebhookListContainerComponent
    ],
    imports: [
        SharedModule
    ],
    exports: [
        WebhookListContainerComponent
    ],
    entryComponents: [
        WebhookListContainerComponent
    ],
    providers: [
        WebhookListDataService
    ]
})
export class WebhookListModule { }
