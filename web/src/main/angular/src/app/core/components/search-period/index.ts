
import { NgModule } from '@angular/core';
import { SharedModule } from 'app/shared';
import { SearchPeriodContainerComponent } from './search-period-container.component';
import { SearchPeriodComponent } from './search-period.component';

@NgModule({
    declarations: [
        SearchPeriodContainerComponent,
        SearchPeriodComponent
    ],
    imports: [
        SharedModule
    ],
    exports: [
        SearchPeriodContainerComponent,
        SearchPeriodComponent
    ],
    providers: []
})
export class SearchPeriodModule {}
