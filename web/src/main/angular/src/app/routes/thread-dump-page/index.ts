import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';

import { SharedModule } from 'app/shared';
import { NoticeModule } from 'app/core/components/notice';
import { ThreadDumpListModule } from 'app/core/components/thread-dump-list';
import { ThreadDumpLogModule } from 'app/core/components/thread-dump-log';
import { ThreadDumpPageComponent } from './thread-dump-page.component';
import { routing } from './thread-dump-page.routing';

@NgModule({
    declarations: [
        ThreadDumpPageComponent
    ],
    imports: [
        SharedModule,
        NoticeModule,
        ThreadDumpListModule,
        ThreadDumpLogModule,
        RouterModule.forChild(routing)
    ],
    exports: [],
    providers: []
})
export class ThreadDumpPageModule { }
