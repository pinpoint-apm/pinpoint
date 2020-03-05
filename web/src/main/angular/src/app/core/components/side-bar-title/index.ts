
import { NgModule } from '@angular/core';
import { MatSelectModule } from '@angular/material/select';

import { SharedModule } from 'app/shared';
import { SideBarTitleComponent } from './side-bar-title.component';
import { SideBarTitleContainerComponent } from './side-bar-title-container.component';

@NgModule({
    declarations: [
        SideBarTitleComponent,
        SideBarTitleContainerComponent
    ],
    imports: [
        SharedModule,
        MatSelectModule
    ],
    exports: [
        SideBarTitleContainerComponent
    ],
    providers: []
})
export class SideBarTitleModule { }
