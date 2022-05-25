import { NgModule } from '@angular/core';

import { SharedModule } from 'app/shared';
import { HelpViewerPopupModule } from 'app/core/components/help-viewer-popup';
import { ApdexScoreComponent } from './apdex-score.component';
import { ApdexScoreContainerComponent } from './apdex-score-container.component';

@NgModule({
    declarations: [
        ApdexScoreComponent,
        ApdexScoreContainerComponent
    ],
    imports: [
        HelpViewerPopupModule,
        SharedModule
    ],
    exports: [
        ApdexScoreContainerComponent,
    ],
    providers: [
    ]
})
export class ApdexScoreModule { }
