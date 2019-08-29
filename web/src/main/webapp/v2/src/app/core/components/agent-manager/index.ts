
import { NgModule } from '@angular/core';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatTooltipModule } from '@angular/material/tooltip';
import { SharedModule } from 'app/shared';
import { AgentManagerContainerComponent } from './agent-manager-container.component';
import { AgentManagerComponent } from './agent-manager.component';
import { AgentManagerDataService } from './agent-manager-data.service';
import { ApplicationListModule } from 'app/core/components/application-list';
import { ServerErrorPopupModule } from 'app/core/components/server-error-popup';

@NgModule({
    declarations: [
        AgentManagerComponent,
        AgentManagerContainerComponent
    ],
    imports: [
        MatSlideToggleModule,
        MatTooltipModule,
        SharedModule,
        ApplicationListModule,
        ServerErrorPopupModule
    ],
    exports: [
        AgentManagerContainerComponent
    ],
    providers: [
        AgentManagerDataService
    ]
})
export class AgentManagerModule { }
