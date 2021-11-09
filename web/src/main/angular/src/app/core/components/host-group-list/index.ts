import { NgModule } from '@angular/core';

import { SharedModule } from 'app/shared';
import { ServerErrorPopupModule } from 'app/core/components/server-error-popup';
import { HostGroupListContainerComponent } from './host-group-list-container.component';
import { HostGroupListDataService } from './host-group-list-data.service';
import { HostGroupListComponent } from './host-group-list.component';

@NgModule({
    declarations: [
        HostGroupListContainerComponent,
        HostGroupListComponent
    ],
    imports: [
        SharedModule,
        ServerErrorPopupModule
    ],
    exports: [
        HostGroupListContainerComponent
    ],
    providers: [
        HostGroupListDataService,
    ],

})
export class HostGroupListModule { }
