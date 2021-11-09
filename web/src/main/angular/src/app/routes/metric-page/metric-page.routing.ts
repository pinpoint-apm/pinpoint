import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';

import { UrlPath, UrlPathId } from 'app/shared/models';
import { UrlRedirectorComponent } from 'app/shared/components/url-redirector/url-redirector.component';
import { EmptyContentsComponent } from 'app/shared/components/empty-contents';
import { MetricPageComponent } from './metric-page.component';
import { MetricContentsContainerComponent } from 'app/core/components/metric-contents/metric-contents-container.component';

const routes: Routes = [
    {
        path: '',
        component: MetricPageComponent,
        children: [
            {
                path: ':' + UrlPathId.HOST_GROUP,
                children: [
                    {
                        path: '',
                        pathMatch: 'full',
                        data: {
                            path: UrlPath.METRIC
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
                                    path: UrlPath.METRIC
                                },
                                component: UrlRedirectorComponent
                            },
                            {
                                path: ':' + UrlPathId.END_TIME,
                                data: {
                                    showRealTimeButton: false,
                                    enableRealTimeMode: false
                                },
                                // TODO: 왼쪽에 agent? 선택했을 때랑 그냥 서버? 전체일때의 라우팅 차이.
                                children: [
                                    {
                                        path: '',
                                        pathMatch: 'full',
                                        // component: ApplicationInspectorContentsContainerComponent
                                    },
                                    {
                                        path: ':' + UrlPathId.HOST,
                                        component: MetricContentsContainerComponent
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
                component: EmptyContentsComponent
            }
        ]
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
export class MetricPageRoutingModule {}
