import { NgModule } from '@angular/core';

import { SharedModule } from 'app/shared';
import { SideNavigationItemComponent } from './side-navigation-item.component';
import { SideNavigationBarContainerComponent } from './side-navigation-bar-container.component';
import { ThemeWidgetModule } from 'app/core/components/theme-widget';

@NgModule({
    declarations: [
        SideNavigationItemComponent,
        SideNavigationBarContainerComponent,
    ],
    imports: [
        SharedModule,
        ThemeWidgetModule,
    ],
    exports: [
        SideNavigationBarContainerComponent
    ],
    providers: []
})
export class SideNavigationBarModule { }
