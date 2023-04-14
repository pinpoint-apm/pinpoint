import { NgModule } from '@angular/core';
import { SharedModule } from 'app/shared';
import { AgGridModule } from 'ag-grid-angular';
import { ThreadDumpListContainerComponent } from './thread-dump-list-container.component';
import { ThreadDumpListComponent } from './thread-dump-list.component';
import { ActiveThreadDumpListDataService } from './active-thread-dump-list-data.service';

@NgModule({
    declarations: [
        ThreadDumpListComponent,
        ThreadDumpListContainerComponent
    ],
    imports: [
        SharedModule,
        AgGridModule.withComponents([])
    ],
    exports: [
        ThreadDumpListContainerComponent
    ],
    providers: [
        ActiveThreadDumpListDataService
    ]
})
export class ThreadDumpListModule { }
