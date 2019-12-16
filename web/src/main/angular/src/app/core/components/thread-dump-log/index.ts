import { NgModule } from '@angular/core';
import { SharedModule } from 'app/shared';

import { ThreadDumpLogContainerComponent } from './thread-dump-log-container.component';
import { ActiveThreadDumpDetailInfoDataService } from './active-thread-dump-detail-info-data.service';

@NgModule({
    declarations: [
        ThreadDumpLogContainerComponent
    ],
    imports: [
        SharedModule
    ],
    exports: [
        ThreadDumpLogContainerComponent
    ],
    providers: [
        ActiveThreadDumpDetailInfoDataService
    ]
})
export class ThreadDumpLogModule {}
