
import { NgModule } from '@angular/core';
import { MatTooltipModule, MatSlideToggleModule } from '@angular/material';
import { SharedModule } from 'app/shared';
import { AgentManagerContainerComponent } from './agent-manager-container.component';
import { AgentManagerComponent } from './agent-manager.component';
import { AgentManagerDataService } from './agent-manager-data.service';

@NgModule({
    declarations: [
        AgentManagerComponent,
        AgentManagerContainerComponent
    ],
    imports: [
        MatSlideToggleModule,
        MatTooltipModule,
        SharedModule
    ],
    exports: [
        AgentManagerContainerComponent
    ],
    providers: [
        AgentManagerDataService
    ]
})
export class AgentManagerModule { }
