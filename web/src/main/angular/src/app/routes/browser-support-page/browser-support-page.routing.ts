import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';

import { BrowserSupportPageComponent } from './browser-support-page.component';

const routes: Routes = [
    {
        path: '',
        component: BrowserSupportPageComponent
    }
];

@NgModule({
    imports: [
        RouterModule.forChild(routes)
    ],
    exports: [
        RouterModule
    ]
})
export class BrowserSupportPageRoutingModule {}
