import { NgModule } from '@angular/core';

import { SharedModule } from 'app/shared';
import { HelpViewerPopupContainerComponent } from './help-viewer-popup-container.component';
import { HelpViewerPopupComponent } from './help-viewer-popup.component';

@NgModule({
    declarations: [
        HelpViewerPopupContainerComponent,
        HelpViewerPopupComponent
    ],
    imports: [
        SharedModule
    ],
    exports: [],
    entryComponents: [
        HelpViewerPopupContainerComponent
    ],
    providers: []
})
export class HelpViewerPopupModule { }
