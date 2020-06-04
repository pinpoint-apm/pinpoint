
import { NgModule } from '@angular/core';
import { ScrollingModule } from '@angular/cdk/scrolling';
import { SharedModule } from 'app/shared';
import { TransactionTimelineComponentV2 } from './transaction-timeline-v2.component';
import { TransactionTimelineContainerComponentV2 } from './transaction-timeline-container-v2.component';

@NgModule({
    declarations: [
        TransactionTimelineComponentV2,
        TransactionTimelineContainerComponentV2
    ],
    imports: [
        SharedModule,
        ScrollingModule
    ],
    exports: [
        TransactionTimelineContainerComponentV2
    ],
    providers: []
})
export class TransactionTimelineModuleV2 { }
