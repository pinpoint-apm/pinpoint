
import { NgModule } from '@angular/core';
import { SharedModule } from 'app/shared';
import { AgentIdDuplicationCheckContainerComponent } from './agent-id-duplication-check-container.component';
import { ApplicationNameDuplicationCheckContainerComponent } from './application-name-duplication-check-container.component';
import { DuplicationCheckComponent } from './duplication-check.component';
import { ApplicationNameDuplicationCheckInteractionService } from './application-name-duplication-check-interaction.service';
import { AgentIdDuplicationCheckInteractionService } from './agent-id-duplication-check-interaction.service';
import { ApplicationNameDuplicationCheckDataService } from './application-name-duplication-check-data.service';
import { AgentIdDuplicationCheckDataService } from './agent-id-duplication-check-data.service';

@NgModule({
    declarations: [
        AgentIdDuplicationCheckContainerComponent,
        ApplicationNameDuplicationCheckContainerComponent,
        DuplicationCheckComponent
    ],
    imports: [
        SharedModule
    ],
    exports: [
        AgentIdDuplicationCheckContainerComponent,
        ApplicationNameDuplicationCheckContainerComponent
    ],
    providers: [
        ApplicationNameDuplicationCheckInteractionService,
        AgentIdDuplicationCheckInteractionService,
        ApplicationNameDuplicationCheckDataService,
        AgentIdDuplicationCheckDataService
    ]
})
export class DuplicationCheckModule {}
