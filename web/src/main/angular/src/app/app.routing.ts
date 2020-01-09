import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';

import { UrlPath } from 'app/shared/models';
import { SystemConfigurationResolverService, ApplicationListResolverService } from 'app/shared/services';
import { PageNotFoundComponent } from 'app/shared/components/page-not-found';

const appRoutes: Routes = [
    {
        path: '',
        children: [
            {
                path: '',
                pathMatch: 'full',
                redirectTo: '/' + UrlPath.MAIN
            },
            {
                path: '',
                resolve: {
                    configuration: SystemConfigurationResolverService,
                    applicationList: ApplicationListResolverService
                },
                children: [
                    {
                        path: UrlPath.CONFIG,
                        loadChildren: () => import('./routes/config-page/index').then(m => m.ConfigPageModule)
                    },
                    {
                        path: UrlPath.BROWSER_NOT_SUPPORT,
                        loadChildren: () => import('./routes/browser-support-page/index').then(m => m.BrowserSupportPageModule)
                    },
                    {
                        path: UrlPath.SCATTER_FULL_SCREEN_MODE,
                        loadChildren: () => import('./routes/scatter-full-screen-mode-page/index').then(m => m.ScatterFullScreenModePageModule)
                    },
                    {
                        path: UrlPath.THREAD_DUMP,
                        loadChildren: () => import('./routes/thread-dump-page/index').then(m => m.ThreadDumpPageModule)
                    },
                    {
                        path: UrlPath.REAL_TIME,
                        loadChildren: () => import('./routes/real-time-page/index').then(m => m.RealTimePageModule)
                    },
                    {
                        path: UrlPath.TRANSACTION_VIEW,
                        loadChildren: () => import('./routes/transaction-view-page/index').then(m => m.TransactionViewPageModule)
                    },
                    {
                        path: UrlPath.TRANSACTION_DETAIL,
                        loadChildren: () => import('./routes/transaction-detail-page/index').then(m => m.TransactionDetailPageModule)
                    },
                    {
                        path: UrlPath.TRANSACTION_LIST,
                        loadChildren: () => import('./routes/transaction-list-page/index').then(m => m.TransactionListPageModule)
                    },
                    {
                        path: UrlPath.INSPECTOR,
                        loadChildren: () => import('./routes/inspector-page/index').then(m => m.InspectorPageModule)
                    },
                    {
                        path: UrlPath.FILTERED_MAP,
                        loadChildren: () => import('./routes/filtered-map-page/index').then(m => m.FilteredMapPageModule)
                    },
                    {
                        path: UrlPath.MAIN,
                        loadChildren: () => import('./routes/main-page/index').then(m => m.MainPageModule)
                    },
                ]
            },
            {
                path: UrlPath.ERROR,
                loadChildren: () => import('./routes/error-page/index').then(m => m.ErrorPageModule)
            },
            {
                path: '**',
                component: PageNotFoundComponent
            }
        ]
    }
];

@NgModule({
    imports: [
        RouterModule.forRoot(appRoutes, { enableTracing: false })
    ],
    exports: [
        RouterModule
    ]
})
export class AppRoutingModule {}
