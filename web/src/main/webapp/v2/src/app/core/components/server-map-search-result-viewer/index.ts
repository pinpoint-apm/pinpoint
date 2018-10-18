
import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ServerMapSearchResultViewerComponent } from './server-map-search-result-viewer.component';
import { ServerMapSearchResultViewerContainerComponent } from './server-map-search-result-viewer-container.component';

@NgModule({
    declarations: [
        ServerMapSearchResultViewerComponent,
        ServerMapSearchResultViewerContainerComponent
    ],
    imports: [
        CommonModule
    ],
    exports: [
        ServerMapSearchResultViewerContainerComponent
    ],
    providers: []
})
export class ServerMapSearchResultViewerModule { }
