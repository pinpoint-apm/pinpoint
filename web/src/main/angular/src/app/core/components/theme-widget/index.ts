
import { NgModule } from '@angular/core';

import { SharedModule } from 'app/shared';
import { ThemeWidgetContainerComponent } from './theme-widget-container.component';

@NgModule({
    declarations: [
      ThemeWidgetContainerComponent,
    ],
    imports: [
        SharedModule
    ],
    exports: [
      ThemeWidgetContainerComponent,
    ],
})
export class ThemeWidgetModule { }
