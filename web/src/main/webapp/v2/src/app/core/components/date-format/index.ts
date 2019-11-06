
import { NgModule } from '@angular/core';
import { SharedModule } from 'app/shared';
import { DateFormatContainerComponent } from './date-format-container.component';
import { DateFormatComponent } from './date-format.component';

@NgModule({
    declarations: [
        DateFormatContainerComponent,
        DateFormatComponent
    ],
    imports: [
        SharedModule
    ],
    exports: [
        DateFormatContainerComponent,
    ],
    providers: [
    ]
})
export class DateFormatModule {}
