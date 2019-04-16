
import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
// import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { MatTooltipModule } from '@angular/material';
import {MatSelectModule} from '@angular/material/select';
import { SideBarTitleComponent } from './side-bar-title.component';
import { SideBarTitleContainerComponent } from './side-bar-title-container.component';

@NgModule({
    declarations: [
        SideBarTitleComponent,
        SideBarTitleContainerComponent
    ],
    imports: [
        CommonModule,
        // BrowserAnimationsModule,
        MatTooltipModule,
        MatSelectModule
    ],
    exports: [
        SideBarTitleContainerComponent
    ],
    providers: []
})
export class SideBarTitleModule { }
