
import { NgModule } from '@angular/core';
import { SharedModule } from 'app/shared';
import { ServerMapInteractionService } from './server-map-interaction.service';
import { ServerMapOverviewComponent } from './server-map-overview.component';
import { ServerMapComponent } from './server-map.component';
import { ServerMapContainerComponent } from './server-map-container.component';
import { ServerMapForFilteredMapContainerComponent } from './server-map-for-filtered-map-container.component';
import { ServerMapForTransactionListContainerComponent } from './server-map-for-transaction-list-container.component';
import { ServerMapForTransactionViewContainerComponent } from './server-map-for-transaction-view-container.component';
import { ServerMapDataService } from './server-map-data.service';
import { ServerMapForFilteredMapDataService } from './server-map-for-filtered-map-data.service';
import { LinkContextPopupModule } from 'app/core/components/link-context-popup';
import { ServerMapContextPopupModule } from 'app/core/components/server-map-context-popup';
import { ServerErrorPopupModule } from 'app/core/components/server-error-popup';

@NgModule({
    declarations: [
        ServerMapComponent,
        ServerMapOverviewComponent,
        ServerMapContainerComponent,
        ServerMapForFilteredMapContainerComponent,
        ServerMapForTransactionListContainerComponent,
        ServerMapForTransactionViewContainerComponent
    ],
    imports: [
        SharedModule,
        ServerErrorPopupModule,
        LinkContextPopupModule,
        ServerMapContextPopupModule
    ],
    exports: [
        ServerMapComponent,
        ServerMapOverviewComponent,
        ServerMapContainerComponent,
        ServerMapForFilteredMapContainerComponent,
        ServerMapForTransactionListContainerComponent,
        ServerMapForTransactionViewContainerComponent
    ],
    providers: [
        ServerMapInteractionService,
        ServerMapDataService,
        ServerMapForFilteredMapDataService
    ]
})
export class ServerMapModule { }
