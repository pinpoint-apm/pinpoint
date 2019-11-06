import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';

import { SharedModule } from 'app/shared';
import { RealTimeModule } from 'app/core/components/real-time';
import { RealTimePageComponent } from './real-time-page.component';
import { routing } from './real-time-page.routing';

@NgModule({
    declarations: [
        RealTimePageComponent
    ],
    imports: [
        SharedModule,
        RealTimeModule,
        RouterModule.forChild(routing)
    ],
    exports: [],
    providers: []
})
export class RealTimePageModule { }
