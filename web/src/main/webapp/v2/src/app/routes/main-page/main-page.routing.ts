
import { Routes } from '@angular/router';
import { UrlPath, UrlPathId } from 'app/shared/models';
import { MainContentsContainerComponent } from 'app/core/components/main-contents/main-contents-container.component';
import { EmptyContentsComponent } from 'app/shared/components/empty-contents';
import { UrlRedirectorComponent } from 'app/shared/components/url-redirector';
import { SystemConfigurationResolverService, ApplicationListResolverService, ServerTimeResolverService } from 'app/shared/services';
import { MainPageComponent } from './main-page.component';

export const routing: Routes = [
    {
        path: '',
        component: MainPageComponent,
        resolve: {
            configuration: SystemConfigurationResolverService,
            applicationList: ApplicationListResolverService
        },
        children: [
            {
                path: ':' + UrlPathId.APPLICATION + '/:' + UrlPathId.PERIOD + '/:' + UrlPathId.END_TIME,
                data: {
                    showRealTimeButton: true,
                    enableRealTimeMode: false
                },
                component: MainContentsContainerComponent
            },
            {
                path: ':' + UrlPathId.APPLICATION + '/' + UrlPath.REAL_TIME,
                resolve: {
                    serverTime: ServerTimeResolverService
                },
                data: {
                    showRealTimeButton: true,
                    enableRealTimeMode: true
                },
                component: MainContentsContainerComponent
            },
            {
                path: ':' + UrlPathId.APPLICATION + '/:' + UrlPathId.PERIOD,
                data: {
                    path: UrlPath.MAIN
                },
                component: UrlRedirectorComponent
            },
            {
                path: ':' + UrlPathId.APPLICATION,
                data: {
                    path: UrlPath.MAIN
                },
                component: UrlRedirectorComponent
            },
            {
                path: '',
                data: {
                    showRealTimeButton: false,
                    enableRealTimeMode: false
                },
                component: EmptyContentsComponent
            }
        ]
    }
];

