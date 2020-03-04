
import { NgModule } from '@angular/core';

import { SharedModule } from 'app/shared';
import { ServerListComponent } from './server-list.component';
import { ServerListContainerComponent } from './server-list-container.component';

@NgModule({
    declarations: [
        ServerListComponent,
        ServerListContainerComponent
    ],
    imports: [
        SharedModule,
    ],
    exports: [
        ServerListContainerComponent
    ],
    providers: []
})
export class ServerListModule { }
