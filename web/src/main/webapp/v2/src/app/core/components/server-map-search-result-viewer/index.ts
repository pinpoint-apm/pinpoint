
import { NgModule } from '@angular/core';

import { SharedModule } from 'app/shared';
import { ServerMapSearchResultViewerComponent } from './server-map-search-result-viewer.component';
import { ServerMapSearchResultViewerContainerComponent } from './server-map-search-result-viewer-container.component';

@NgModule({
    declarations: [
        ServerMapSearchResultViewerComponent,
        ServerMapSearchResultViewerContainerComponent
    ],
    imports: [
        SharedModule
    ],
    exports: [
        ServerMapSearchResultViewerContainerComponent
    ],
    providers: []
})
export class ServerMapSearchResultViewerModule { }
