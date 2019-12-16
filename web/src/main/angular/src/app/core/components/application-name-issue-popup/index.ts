import { NgModule } from '@angular/core';

import { SharedModule } from 'app/shared';
import { ApplicationNameIssuePopupContainerComponent } from 'app/core/components/application-name-issue-popup/application-name-issue-popup-container.component';
import { ApplicationNameIssuePopupComponent } from 'app/core/components/application-name-issue-popup/application-name-issue-popup.component';

@NgModule({
    declarations: [
        ApplicationNameIssuePopupContainerComponent,
        ApplicationNameIssuePopupComponent
    ],
    imports: [
        SharedModule
    ],
    exports: [],
    entryComponents: [
        ApplicationNameIssuePopupContainerComponent,
    ],
    providers: [],
})
export class ApplicationNameIssuePopupModule { }
