
import { NgModule } from '@angular/core';
import { SharedModule } from 'app/shared';
import { InspectorChartModule } from 'app/core/components/inspector-chart';
import { ServerMapModule } from 'app/core/components/server-map';
import { TransactionViewTopContentsContainerComponent } from './transaction-view-top-contents-container.component';

@NgModule({
    declarations: [
        TransactionViewTopContentsContainerComponent
    ],
    imports: [
        SharedModule,
        InspectorChartModule,
        ServerMapModule
    ],
    exports: [
        TransactionViewTopContentsContainerComponent
    ],
    providers: []
})
export class TransactionViewTopContentsModule { }
