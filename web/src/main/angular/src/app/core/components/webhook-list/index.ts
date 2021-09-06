import { NgModule } from '@angular/core';
import { SharedModule } from 'app/shared';
import { WebhookListComponent } from './webhook-list.component';
import { WebhookListCreateAndUpdateComponent } from './webhook-list-create-and-update.component';
import { WebhookListContainerComponent } from './webhook-list-container.component';
import { WebhookListDataService } from './webhook-list-data.service';

@NgModule({
    declarations: [
        WebhookListComponent,
        WebhookListContainerComponent,
        WebhookListCreateAndUpdateComponent,
    ],
    imports: [
        SharedModule
    ],
    exports: [
        WebhookListContainerComponent,
        WebhookListCreateAndUpdateComponent,
    ],
    entryComponents: [
        WebhookListContainerComponent
    ],
    providers: [
        WebhookListDataService
    ]
})
export class WebhookListModule { }
