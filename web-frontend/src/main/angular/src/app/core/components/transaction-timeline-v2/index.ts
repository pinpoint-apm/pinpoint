
import { NgModule } from '@angular/core';
import { ScrollingModule } from '@angular/cdk/scrolling';
import { SharedModule } from 'app/shared';
import { TransactionTimelineV2Component } from './transaction-timeline-v2.component';
import { TransactionTimelineV2ContainerComponent } from './transaction-timeline-v2-container.component';

@NgModule({
    declarations: [
        TransactionTimelineV2Component,
        TransactionTimelineV2ContainerComponent
    ],
    imports: [
        SharedModule,
        ScrollingModule
    ],
    exports: [
        TransactionTimelineV2ContainerComponent
    ],
    providers: []
})
export class TransactionTimelineV2Module { }
