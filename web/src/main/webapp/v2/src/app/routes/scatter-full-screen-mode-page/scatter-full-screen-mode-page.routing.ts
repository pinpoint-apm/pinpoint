import { Routes } from '@angular/router';
import { UrlPath, UrlPathId } from 'app/shared/models';
import { ScatterChartForFullScreenModeContainerComponent } from 'app/core/components/scatter-chart/scatter-chart-for-full-screen-mode-container.component';
import { UrlRedirectorComponent } from 'app/shared/components/url-redirector/url-redirector.component';
import { SystemConfigurationResolverService, ApplicationListResolverService, ServerTimeResolverService } from 'app/shared/services';
import { ScatterFullScreenModePageComponent } from './scatter-full-screen-mode-page.component';

export const routing: Routes = [
    {
        path: '',
        component: ScatterFullScreenModePageComponent,
        resolve: {
            configuration: SystemConfigurationResolverService,
            applicationList: ApplicationListResolverService
        },
        children: [
            {
                path: '',
                redirectTo: '/' + UrlPath.MAIN,
                pathMatch: 'full'
            },
            {
                path: ':' + UrlPathId.APPLICATION,
                data: {
                    path: UrlPath.MAIN
                },
                component: UrlRedirectorComponent
            },
            {
                path: ':' + UrlPathId.APPLICATION + '/' + UrlPathId.REAL_TIME,
                resolve: {
                    serverTime: ServerTimeResolverService
                },
                data: {
                    enableRealTimeMode: true
                },
                component: ScatterChartForFullScreenModeContainerComponent
            },
            {
                path: ':' + UrlPathId.APPLICATION + '/:' + UrlPathId.PERIOD,
                data: {
                    path: UrlPath.MAIN
                },
                component: UrlRedirectorComponent
            },
            {
                path: ':' + UrlPathId.APPLICATION + '/:' + UrlPathId.PERIOD + '/:' + UrlPathId.END_TIME,
                component: ScatterChartForFullScreenModeContainerComponent
            },
            {
                path: ':' + UrlPathId.APPLICATION + '/:' + UrlPathId.PERIOD + '/:' + UrlPathId.END_TIME + '/:' + UrlPathId.AGENT_ID,
                component: ScatterChartForFullScreenModeContainerComponent
            }
        ]
    }
];
