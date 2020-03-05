import { NgModule } from '@angular/core';

import { SharedModule } from 'app/shared';
import { NoticeContainerComponent } from 'app/core/components/notice/notice-container.component';

@NgModule({
    imports: [
        SharedModule,
    ],
    exports: [
        NoticeContainerComponent
    ],
    declarations: [
        NoticeContainerComponent
    ],
    providers: [],
})
export class NoticeModule { }
