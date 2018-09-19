
import { NgModule } from '@angular/core';
import { NguiDatetimePickerModule } from '@ngui/datetime-picker';
import { SharedModule } from 'app/shared';

import { PeriodSelectorUsingReservedTimeComponent } from './period-selector-using-reserved-time.component';
import { PeriodSelectorUsingCalendarComponent } from './period-selector-using-calendar.component';
import { PeriodSelectorComponent } from './period-selector.component';
import { PeriodSelectorContainerComponent } from './period-selector-container.component';

@NgModule({
    declarations: [
        PeriodSelectorUsingReservedTimeComponent,
        PeriodSelectorUsingCalendarComponent,
        PeriodSelectorComponent,
        PeriodSelectorContainerComponent
    ],
    imports: [
        NguiDatetimePickerModule,
        SharedModule
    ],
    exports: [
        PeriodSelectorContainerComponent
    ],
    providers: []
})
export class PeriodSelectorModule { }
