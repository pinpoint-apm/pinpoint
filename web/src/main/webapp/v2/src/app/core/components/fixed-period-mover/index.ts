
import { NgModule } from '@angular/core';
import { SharedModule } from 'app/shared';
import { FixedPeriodMoverComponent } from './fixed-period-mover.component';
import { FixedPeriodMoverContainerComponent } from './fixed-period-mover-container.component';

@NgModule({
    declarations: [
        FixedPeriodMoverComponent,
        FixedPeriodMoverContainerComponent
    ],
    imports: [
        SharedModule
    ],
    exports: [
        FixedPeriodMoverContainerComponent
    ],
    providers: []
})
export class FixedPeriodMoverModule { }
