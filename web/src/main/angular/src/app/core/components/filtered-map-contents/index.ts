
import { NgModule } from '@angular/core';

import { SharedModule } from 'app/shared';
import { FixedPeriodMoverModule } from 'app/core/components/fixed-period-mover';
import { ServerMapSearchResultViewerModule } from 'app/core/components/server-map-search-result-viewer';
import { ServerMapModule } from 'app/core/components/server-map';
import { FilteredMapContentsContainerComponent } from './filtered-map-contents-container.component';
import { HelpViewerPopupModule } from 'app/core/components/help-viewer-popup';

@NgModule({
    declarations: [
        FilteredMapContentsContainerComponent
    ],
    imports: [
        SharedModule,
        FixedPeriodMoverModule,
        ServerMapSearchResultViewerModule,
        ServerMapModule,
        HelpViewerPopupModule
    ],
    exports: [
        FilteredMapContentsContainerComponent
    ],
    providers: []
})
export class FilteredMapContentsModule { }
