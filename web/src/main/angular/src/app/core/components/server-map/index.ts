
import { NgModule } from '@angular/core';
import { SharedModule } from 'app/shared';
import { ServerMapInteractionService } from './server-map-interaction.service';
import { ServerMapComponent } from './server-map.component';
import { ServerMapContainerComponent } from './server-map-container.component';
import { ServerMapOthersContainerComponent } from './server-map-others-container.component';
import { ServerMapForFilteredMapContainerComponent } from './server-map-for-filtered-map-container.component';
import { ServerMapDataService } from './server-map-data.service';
import { ServerMapForFilteredMapDataService } from './server-map-for-filtered-map-data.service';
import { ServerMapChangeNotificationService } from './server-map-change-notification.service';
import { LinkContextPopupModule } from 'app/core/components/link-context-popup';
import { ServerMapContextPopupModule } from 'app/core/components/server-map-context-popup';
import { ServerErrorPopupModule } from 'app/core/components/server-error-popup';

@NgModule({
    declarations: [
        ServerMapComponent,
        ServerMapContainerComponent,
        ServerMapOthersContainerComponent,
        ServerMapForFilteredMapContainerComponent
    ],
    imports: [
        SharedModule,
        ServerErrorPopupModule,
        LinkContextPopupModule,
        ServerMapContextPopupModule
    ],
    exports: [
        ServerMapComponent,
        ServerMapContainerComponent,
        ServerMapOthersContainerComponent,
        ServerMapForFilteredMapContainerComponent
    ],
    providers: [
        ServerMapInteractionService,
        ServerMapChangeNotificationService,
        ServerMapDataService,
        ServerMapForFilteredMapDataService
    ]
})
export class ServerMapModule { }
