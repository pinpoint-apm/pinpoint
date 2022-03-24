import { NgModule } from '@angular/core';

import { SharedModule } from 'app/shared';
import { HelpViewerPopupModule } from 'app/core/components/help-viewer-popup';
import { ApdexScoreComponent } from './apdex-score.component';

@NgModule({
    declarations: [
        ApdexScoreComponent,
    ],
    imports: [
        HelpViewerPopupModule,
        SharedModule
    ],
    exports: [
        ApdexScoreComponent,
    ],
    providers: [
    ]
})
export class ApdexScoreModule { }
