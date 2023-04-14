
import { NgModule } from '@angular/core';
import { ScrollingModule } from '@angular/cdk/scrolling';
import { SharedModule } from 'app/shared';
import { TransactionTimelineComponent } from './transaction-timeline.component';
import { TransactionTimelineContainerComponent } from './transaction-timeline-container.component';

@NgModule({
    declarations: [
        TransactionTimelineComponent,
        TransactionTimelineContainerComponent
    ],
    imports: [
        SharedModule,
        ScrollingModule
    ],
    exports: [
        TransactionTimelineContainerComponent
    ],
    providers: []
})
export class TransactionTimelineModule { }
