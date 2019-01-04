import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { NgModule } from '@angular/core';
import { HttpClientModule } from '@angular/common/http';
import { RouterModule, Routes } from '@angular/router';
import { StoreModule } from '@ngrx/store';
import { LocalStorageModule } from 'angular-2-local-storage';
import { HttpClient } from '@angular/common/http';
import { APP_BASE_HREF } from '@angular/common';
import { COMPOSITION_BUFFER_MODE } from '@angular/forms';

import { TranslateModule, TranslateLoader } from '@ngx-translate/core';
import { TranslateHttpLoader } from '@ngx-translate/http-loader';

import { httpInterceptorProviders } from './core/httpInterceptor';

import { AppComponent } from './app.component';
import { PageNotFoundComponent } from './shared/components/page-not-found';

import { reducers } from './shared/store';
import { UrlPath } from './shared/models';
import { SERVER_MAP_TYPE, ServerMapType } from 'app/core/components/server-map/class/server-map-factory';
import { WindowRefService } from './shared/services/window-ref.service';

export function HttpLoaderFactory(http: HttpClient) {
    return new TranslateHttpLoader(http, 'assets/i18n/', '.json');
}

export const appRoutes: Routes = [
    {
        path: UrlPath.CONFIG,
        loadChildren: './routes/config-page/index#ConfigPageModule'
    },
    {
        path: UrlPath.ADMIN,
        loadChildren: './routes/admin-page/index#AdminPageModule'
    },
    {
        path: UrlPath.ERROR,
        loadChildren: './routes/error-page/index#ErrorPageModule'
    },
    {
        path: UrlPath.BROWSER_NOT_SUPPORT,
        loadChildren: './routes/browser-support-page/index#BrowserSupportPageModule'
    },
    {
        path: UrlPath.SCATTER_FULL_SCREEN_MODE,
        loadChildren: './routes/scatter-full-screen-mode-page/index#ScatterFullScreenModePageModule'
    },
    {
        path: UrlPath.THREAD_DUMP,
        loadChildren: './routes/thread-dump-page/index#ThreadDumpPageModule'
    },
    {
        path: UrlPath.REAL_TIME,
        loadChildren: './routes/real-time-page/index#RealTimePageModule'
    },
    {
        path: UrlPath.TRANSACTION_VIEW,
        loadChildren: './routes/transaction-view-page/index#TransactionViewPageModule'
    },
    {
        path: UrlPath.TRANSACTION_DETAIL,
        loadChildren: './routes/transaction-detail-page/index#TransactionDetailPageModule'
    },
    {
        path: UrlPath.TRANSACTION_LIST,
        loadChildren: './routes/transaction-list-page/index#TransactionListPageModule'
    },
    {
        path: UrlPath.INSPECTOR,
        loadChildren: './routes/inspector-page/index#InspectorPageModule'
    },
    {
        path: UrlPath.FILTERED_MAP,
        loadChildren: './routes/filtered-map-page/index#FilteredMapPageModule'
    },
    {
        path: UrlPath.MAIN,
        loadChildren: './routes/main-page/index#MainPageModule'
    },
    {
        path: '',
        redirectTo: '/' + UrlPath.MAIN,
        pathMatch: 'full'
    },
    {
        path: '**',
        component: PageNotFoundComponent
    }
];

@NgModule({
    declarations: [
        AppComponent,
        PageNotFoundComponent,
    ],
    imports: [
        BrowserAnimationsModule,
        HttpClientModule,
        StoreModule.forRoot(reducers, {}),
        LocalStorageModule.withConfig({
            prefix: 'pp',
            storageType: 'localStorage'
        }),
        TranslateModule.forRoot({
            loader: {
                provide: TranslateLoader,
                useFactory: HttpLoaderFactory,
                deps: [HttpClient]
            }
        }),
        RouterModule.forRoot(appRoutes, { enableTracing: false })
    ],
    providers: [
        WindowRefService,
        httpInterceptorProviders,
        { provide: APP_BASE_HREF, useValue: window.document.querySelector('base').getAttribute('href') },
        { provide: COMPOSITION_BUFFER_MODE, useValue: false },
        { provide: SERVER_MAP_TYPE, useValue: ServerMapType.VISJS }
    ],
    bootstrap: [AppComponent]
})
export class AppModule {}
