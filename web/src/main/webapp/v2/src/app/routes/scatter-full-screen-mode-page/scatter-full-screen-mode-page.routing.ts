import { Routes } from '@angular/router';

import { UrlPath, UrlPathId } from 'app/shared/models';
import { ScatterChartForFullScreenModeContainerComponent } from 'app/core/components/scatter-chart/scatter-chart-for-full-screen-mode-container.component';
import { UrlRedirectorComponent } from 'app/shared/components/url-redirector/url-redirector.component';
import { ServerTimeResolverService } from 'app/shared/services';
import { ScatterFullScreenModePageComponent } from './scatter-full-screen-mode-page.component';

export const routing: Routes = [
    {
        path: '',
        component: ScatterFullScreenModePageComponent,
        children: [
            {
                path: UrlPath.REAL_TIME,
                children: [
                    {
                        path: '',
                        pathMatch: 'full',
                        redirectTo: '/' + UrlPath.MAIN
                    },
                    {
                        path: ':' + UrlPathId.APPLICATION,
                        resolve: {
                            serverTime: ServerTimeResolverService
                        },
                        data: {
                            showRealTimeButton: true,
                            enableRealTimeMode: true
                        },
                        children: [
                            {
                                path: '',
                                pathMatch: 'full',
                                component: ScatterChartForFullScreenModeContainerComponent
                            },
                            {
                                path: ':' + UrlPathId.AGENT_ID,
                                component: ScatterChartForFullScreenModeContainerComponent
                            }
                        ]
                    }
                ]
            },
            // ! URL 순서 바꾸면서 사용자들이 기존의 url로 접근했을때 redirect해주려고 임시로 추가해둠.
            {
                path: ':' + UrlPathId.APPLICATION + '/' + UrlPathId.REAL_TIME,
                pathMatch: 'full',
                redirectTo: '/' + UrlPath.SCATTER_FULL_SCREEN_MODE + '/' + UrlPath.REAL_TIME + '/:' + UrlPathId.APPLICATION,
            },
            {
                path: ':' + UrlPathId.APPLICATION,
                children: [
                    {
                        path: '',
                        pathMatch: 'full',
                        data: {
                            path: UrlPath.SCATTER_FULL_SCREEN_MODE
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
                                    path: UrlPath.SCATTER_FULL_SCREEN_MODE
                                },
                                component: UrlRedirectorComponent
                            },
                            {
                                path: ':' + UrlPathId.END_TIME,
                                data: {
                                    showRealTimeButton: true,
                                    enableRealTimeMode: false
                                },
                                children: [
                                    {
                                        path: '',
                                        pathMatch: 'full',
                                        component: ScatterChartForFullScreenModeContainerComponent
                                    },
                                    {
                                        path: ':' + UrlPathId.AGENT_ID,
                                        component: ScatterChartForFullScreenModeContainerComponent
                                    }
                                ]
                            }
                        ]
                    }
                ]
            },
            {
                path: '',
                pathMatch: 'full',
                redirectTo: '/' + UrlPath.MAIN
            }
        ]
    }
];
