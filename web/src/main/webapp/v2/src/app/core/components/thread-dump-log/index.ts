import { NgModule } from '@angular/core';
import { SharedModule } from 'app/shared';

import { ThreadDumpLogContainerComponent } from './thread-dump-log-container.component';
import { ThreadDumpLogInteractionService } from './thread-dump-log-interaction.service';
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
        ThreadDumpLogInteractionService,
        ActiveThreadDumpDetailInfoDataService
    ]
})
export class ThreadDumpLogModule {}
