import { NgModule } from '@angular/core';
import { SharedModule } from 'app/shared';

import { ApplicationInspectorUsageGuideContainerComponent } from './application-inspector-usage-guide-container.component';

@NgModule({
    declarations: [
        ApplicationInspectorUsageGuideContainerComponent
    ],
    imports: [
        SharedModule
    ],
    exports: [
        ApplicationInspectorUsageGuideContainerComponent
    ],
    providers: [],
})
export class ApplicationInspectorUsageGuideModule { }
