
import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
// import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { MatTooltipModule } from '@angular/material';
import { ServerAndAgentListComponent } from './server-and-agent-list.component';
import { ServerAndAgentListContainerComponent } from './server-and-agent-list-container.component';
import { ServerAndAgentListDataService } from './server-and-agent-list-data.service';

@NgModule({
    declarations: [
        ServerAndAgentListComponent,
        ServerAndAgentListContainerComponent
    ],
    imports: [
        CommonModule,
        // BrowserAnimationsModule,
        MatTooltipModule
    ],
    exports: [
        ServerAndAgentListContainerComponent
    ],
    providers: [
        ServerAndAgentListDataService
    ]
})
export class ServerAndAgentListModule { }
