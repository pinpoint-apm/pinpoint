import {NgModule} from '@angular/core';

import {SharedModule} from 'app/shared';
import {HelpViewerPopupModule} from 'app/core/components/help-viewer-popup';
import {ApdexScoreComponent} from './apdex-score.component';
import {ApdexScoreContainerComponent} from './apdex-score-container.component';
import {ApdexScoreGuideComponent} from './apdex-score-guide.component';

@NgModule({
    declarations: [
        ApdexScoreComponent,
        ApdexScoreContainerComponent,
        ApdexScoreGuideComponent
    ],
    imports: [
        HelpViewerPopupModule,
        SharedModule
    ],
    exports: [
        ApdexScoreContainerComponent,
        ApdexScoreGuideComponent
    ],
    entryComponents: [
        ApdexScoreGuideComponent
    ],
    providers: []
})
export class ApdexScoreModule {
}
