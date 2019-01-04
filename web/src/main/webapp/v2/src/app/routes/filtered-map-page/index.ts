import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';

import { routing } from './filtered-map-page.routing';
import { SharedModule } from 'app/shared';
import { NoticeModule } from 'app/core/components/notice';
import { DataLoadIndicatorModule } from 'app/core/components/data-load-indicator';
import { StateButtonModule } from 'app/core/components/state-button';
import { ConfigurationIconModule } from 'app/core/components/configuration-icon';
import { FilteredMapContentsModule } from 'app/core/components/filtered-map-contents';
import { SideBarModule } from 'app/core/components/side-bar';

import { FilteredMapPageComponent } from './filtered-map-page.component';

@NgModule({
    declarations: [
        FilteredMapPageComponent
    ],
    imports: [
        SharedModule,
        NoticeModule,
        DataLoadIndicatorModule,
        StateButtonModule,
        ConfigurationIconModule,
        FilteredMapContentsModule,
        SideBarModule,
        RouterModule.forChild(routing)
    ],
    exports: [
        RouterModule
    ],
    providers: []
})
export class FilteredMapPageModule { }
