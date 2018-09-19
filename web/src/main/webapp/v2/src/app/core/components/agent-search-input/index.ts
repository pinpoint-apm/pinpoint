
import { NgModule } from '@angular/core';

import { SharedModule } from 'app/shared';
import { AgentSearchInputContainerComponent } from './agent-search-input-container.component';
import { HelpViewerPopupModule } from 'app/core/components/help-viewer-popup';

@NgModule({
    declarations: [
        AgentSearchInputContainerComponent
    ],
    imports: [
        SharedModule,
        HelpViewerPopupModule
    ],
    exports: [
        AgentSearchInputContainerComponent
    ],
    providers: []
})
export class AgentSearchInputModule { }
