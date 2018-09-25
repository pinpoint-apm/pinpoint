
import { NgModule } from '@angular/core';
import { SharedModule } from 'app/shared';
import { TimezoneSelectContainerComponent } from './timezone-select-container.component';
import { TimezoneSelectComponent } from './timezone-select.component';

@NgModule({
    declarations: [
        TimezoneSelectContainerComponent,
        TimezoneSelectComponent
    ],
    imports: [
        SharedModule
    ],
    exports: [
        TimezoneSelectContainerComponent,
    ],
    providers: [
    ]
})
export class TimezoneModule {}
