
import { NgModule } from '@angular/core';
import { MatTooltipModule } from '@angular/material';
import { SharedModule } from 'app/shared';
import { TransactionShortInfoContainerComponent } from './transaction-short-info-container.component';

@NgModule({
    declarations: [
        TransactionShortInfoContainerComponent
    ],
    imports: [
        MatTooltipModule,
        SharedModule
    ],
    exports: [
        TransactionShortInfoContainerComponent,
    ],
    providers: [
    ]
})
export class TransactionShortInfoModule {}
