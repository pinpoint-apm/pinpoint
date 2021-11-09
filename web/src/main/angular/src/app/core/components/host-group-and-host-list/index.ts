import { NgModule } from '@angular/core';

import { SharedModule } from 'app/shared';
import { HostGroupAndHostListComponent } from './host-group-and-host-list.component';
import { HostGroupAndHostListContainerComponent } from './host-group-and-host-list-container.component';
import { ServerErrorPopupModule } from 'app/core/components/server-error-popup';
import { HostGroupAndHostListDataService } from './host-group-and-host-list-data.service';

@NgModule({
    declarations: [
        HostGroupAndHostListComponent,
        HostGroupAndHostListContainerComponent
    ],
    imports: [
        SharedModule,
        ServerErrorPopupModule,
    ],
    exports: [
        HostGroupAndHostListContainerComponent
    ],
    providers: [
        HostGroupAndHostListDataService
    ]
})
export class HostGroupAndHostListModule { }
