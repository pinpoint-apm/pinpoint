
import { NgModule } from '@angular/core';
import { SharedModule } from 'app/shared';
import { TransactionTimelineComponent } from './transaction-timeline.component';
import { TransactionTimelineContainerComponent } from './transaction-timeline-container.component';

@NgModule({
    declarations: [
        TransactionTimelineComponent,
        TransactionTimelineContainerComponent
    ],
    imports: [
        SharedModule
    ],
    exports: [
        TransactionTimelineContainerComponent
    ],
    providers: []
})
export class TransactionTimelineModule { }
