
import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
// import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { MatTooltipModule } from '@angular/material';
import { ServerListComponent } from './server-list.component';
import { ServerListContainerComponent } from './server-list-container.component';

@NgModule({
    declarations: [
        ServerListComponent,
        ServerListContainerComponent
    ],
    imports: [
        CommonModule,
        // BrowserAnimationsModule,
        MatTooltipModule
    ],
    exports: [
        ServerListContainerComponent
    ],
    providers: []
})
export class ServerListModule { }
