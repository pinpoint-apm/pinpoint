
import { NgModule } from '@angular/core';
import { SharedModule } from 'app/shared';
import { TransactionShortInfoContainerComponent } from './transaction-short-info-container.component';

@NgModule({
    declarations: [
        TransactionShortInfoContainerComponent
    ],
    imports: [
        SharedModule
    ],
    exports: [
        TransactionShortInfoContainerComponent,
    ],
    providers: [
    ]
})
export class TransactionShortInfoModule {}
