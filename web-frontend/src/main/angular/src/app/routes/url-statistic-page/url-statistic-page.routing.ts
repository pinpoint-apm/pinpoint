import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';

import { UrlStatisticContentsContainerComponent } from 'app/core/components/url-statistic-contents/url-statistic-contents-container.component';
import { EmptyContentsComponent } from 'app/shared/components/empty-contents';
import { UrlRedirectorComponent } from 'app/shared/components/url-redirector';
import { UrlPath, UrlPathId } from 'app/shared/models';
import { UrlStatisticPageComponent } from './url-statistic-page.component';

const routes: Routes = [
    {
        path: '',
        component: UrlStatisticPageComponent,
        children: [
            {
                path: ':' + UrlPathId.APPLICATION,
                children: [
                    {
                        path: '',
                        pathMatch: 'full',
                        data: {
                            path: UrlPath.URL_STATISTIC
                        },
                        component: UrlRedirectorComponent
                    },
                    {
                        path: ':' + UrlPathId.PERIOD,
                        children: [
                            {
                                path: '',
                                pathMatch: 'full',
                                data: {
                                    path: UrlPath.URL_STATISTIC
                                },
                                component: UrlRedirectorComponent
                            },
                            {
                                path: ':' + UrlPathId.END_TIME,
                                data: {
                                    showRealTimeButton: false,
                                    enableRealTimeMode: false
                                },
                                children: [
                                    {
                                        path: '',
                                        pathMatch: 'full',
                                        component: UrlStatisticContentsContainerComponent
                                    },
                                    {
                                        path: ':' + UrlPathId.AGENT_ID,
                                        component: UrlStatisticContentsContainerComponent
                                    }
                                ]
                            }
                        ]
                    },
                ]
            },
            {
                path: '',
                pathMatch: 'full',
                data: {
                    showRealTimeButton: false,
                    enableRealTimeMode: false
                },
                component: EmptyContentsComponent
            }
        ]
    },
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
