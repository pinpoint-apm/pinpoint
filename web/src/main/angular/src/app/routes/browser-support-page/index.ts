import { NgModule } from '@angular/core';
import { SharedModule } from 'app/shared';
import { BrowserSupportPageRoutingModule } from './browser-support-page.routing';
import { BrowserSupportPageComponent } from './browser-support-page.component';

@NgModule({
    declarations: [
        BrowserSupportPageComponent
    ],
    imports: [
        SharedModule,
        BrowserSupportPageRoutingModule
    ],
    exports: [],
    providers: []
})
export class BrowserSupportPageModule {}
