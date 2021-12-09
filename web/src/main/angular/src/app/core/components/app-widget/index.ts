import { NgModule } from '@angular/core';

import { SharedModule } from 'app/shared';
import { ApplicationListModule } from 'app/core/components/application-list';
import { ServerMapOptionsModule } from 'app/core/components/server-map-options';
import { PeriodSelectorModule } from 'app/core/components/period-selector';
import { TransactionIdSearchModule} from 'app/core/components/transaction-id-search';

import { AppWidgetComponent } from './app-widget.component';


@NgModule({
    declarations: [
        AppWidgetComponent,
    ],
    imports: [
        SharedModule,
        ApplicationListModule,
        ServerMapOptionsModule,
        PeriodSelectorModule,
        TransactionIdSearchModule,
    ],
    exports: [
      AppWidgetComponent,
    ],
})
export class AppWidgetModule { }
