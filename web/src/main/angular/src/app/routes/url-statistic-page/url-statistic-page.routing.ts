import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';

import { UrlStatisticPageComponent } from './url-statistic-page.component';

const routes: Routes = [
    {
        path: '',
        component: UrlStatisticPageComponent
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
export class UrlStatisticPageRoutingModule {}
