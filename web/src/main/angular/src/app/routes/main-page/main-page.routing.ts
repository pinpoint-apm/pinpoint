import { Routes } from '@angular/router';

import { UrlPath, UrlPathId } from 'app/shared/models';
import { MainContentsContainerComponent } from 'app/core/components/main-contents/main-contents-container.component';
import { EmptyContentsComponent } from 'app/shared/components/empty-contents';
import { UrlRedirectorComponent } from 'app/shared/components/url-redirector';
import { ServerTimeResolverService } from 'app/shared/services';
import { MainPageComponent } from './main-page.component';

export const routing: Routes = [
    {
        path: '',
        component: MainPageComponent,
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
                        component: MainContentsContainerComponent
                    }
                ]
            },
            // ! URL 순서 바꾸면서 사용자들이 기존의 url로 접근했을때 redirect해주려고 임시로 추가해둠.
            {
                path: ':' + UrlPathId.APPLICATION + '/' + UrlPathId.REAL_TIME,
                pathMatch: 'full',
                redirectTo: '/' + UrlPath.MAIN + '/' + UrlPath.REAL_TIME + '/:' + UrlPathId.APPLICATION
            },
            {
                path: ':' + UrlPathId.APPLICATION,
                children: [
                    // {
                    //     path: UrlPath.REAL_TIME,
                    //     pathMatch: 'full',
                    //     redirectTo: '/' + UrlPath.MAIN + '/' + UrlPath.REAL_TIME + '/:' + UrlPathId.APPLICATION
                    // },
                    {
                        path: '',
                        pathMatch: 'full',
                        data: {
                            path: UrlPath.MAIN
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
                                    path: UrlPath.MAIN
                                },
                                component: UrlRedirectorComponent
                            },
                            {
                                path: ':' + UrlPathId.END_TIME,
                                data: {
                                    showRealTimeButton: true,
                                    enableRealTimeMode: false
                                },
                                component: MainContentsContainerComponent
                            }
                        ]
                    }
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
