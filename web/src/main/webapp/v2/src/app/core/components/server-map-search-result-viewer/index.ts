
import { NgModule } from '@angular/core';

import { SharedModule } from 'app/shared';
import { ServerMapSearchResultViewerContainerComponent } from './server-map-search-result-viewer-container.component';

@NgModule({
    declarations: [
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
