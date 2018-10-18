import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';

import { SharedModule } from 'app/shared';
import { ScatterChartModule } from 'app/core/components/scatter-chart';
import { ApplicationListModule } from 'app/core/components/application-list';
import { NoticeModule } from 'app/core/components/notice';
import { ScatterFullScreenModePageComponent } from './scatter-full-screen-mode-page.component';
import { routing } from './scatter-full-screen-mode-page.routing';

@NgModule({
    declarations: [
        ScatterFullScreenModePageComponent
    ],
    imports: [
        RouterModule.forChild(routing),
        SharedModule,
        ApplicationListModule,
        ScatterChartModule,
        NoticeModule,
    ],
    exports: [
    ],
    providers: []
})
export class ScatterFullScreenModePageModule {}
