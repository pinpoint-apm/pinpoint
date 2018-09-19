
import { NgModule } from '@angular/core';

import { SharedModule } from 'app/shared';
import { AgentInfoContainerComponent } from './agent-info-container.component';
import { AgentInfoComponent } from './agent-info.component';
import { AgentInfoDataService } from './agent-info-data.service';
import { ApplicationNameIssuePopupModule } from 'app/core/components/application-name-issue-popup';

@NgModule({
    declarations: [
        AgentInfoComponent,
        AgentInfoContainerComponent
    ],
    imports: [
        SharedModule,
        ApplicationNameIssuePopupModule
    ],
    exports: [
        AgentInfoContainerComponent
    ],
    providers: [
        AgentInfoDataService
    ]
})
export class AgentInfoModule { }
