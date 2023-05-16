import {NgModule} from '@angular/core';

import {SharedModule} from 'app/shared';
import {FixedPeriodMoverModule} from 'app/core/components/fixed-period-mover';
import {ServerMapSearchResultViewerModule} from 'app/core/components/server-map-search-result-viewer';
import {ServerMapModule} from 'app/core/components/server-map';
import {MainContentsContainerComponent} from './main-contents-container.component';
import {HelpViewerPopupModule} from 'app/core/components/help-viewer-popup';
import {RealTimeModule} from 'app/core/components/real-time';
import {SideBarModule} from 'app/core/components/side-bar';

@NgModule({
    declarations: [
        MainContentsContainerComponent
    ],
    imports: [
        SharedModule,
        FixedPeriodMoverModule,
        ServerMapSearchResultViewerModule,
        ServerMapModule,
        HelpViewerPopupModule,
        RealTimeModule,
        SideBarModule,
    ],
    exports: [
        MainContentsContainerComponent
    ],
    providers: []
})
export class MainContentsModule {
}
