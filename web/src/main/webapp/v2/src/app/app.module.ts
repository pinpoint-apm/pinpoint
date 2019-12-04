import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { NgModule } from '@angular/core';
import { HttpClientModule } from '@angular/common/http';
import { StoreModule } from '@ngrx/store';
import { LocalStorageModule } from 'angular-2-local-storage';
import { HttpClient } from '@angular/common/http';
import { APP_BASE_HREF } from '@angular/common';
import { COMPOSITION_BUFFER_MODE } from '@angular/forms';
import { TranslateModule, TranslateLoader } from '@ngx-translate/core';
import { TranslateHttpLoader } from '@ngx-translate/http-loader';

import { httpInterceptorProviders } from 'app/core/httpInterceptor';
import { AppComponent } from 'app/app.component';
import { PageNotFoundComponent } from 'app/shared/components/page-not-found';
import { SharedModule } from 'app/shared';
import { reducers } from 'app/shared/store';
import { AppRoutingModule } from 'app/app.routing';
import { SERVER_MAP_TYPE, ServerMapType } from 'app/core/components/server-map/class/server-map-factory';

export function HttpLoaderFactory(http: HttpClient) {
    return new TranslateHttpLoader(http, 'assets/i18n/', '.json');
}

@NgModule({
    declarations: [
        AppComponent,
        PageNotFoundComponent,
    ],
    imports: [
        BrowserAnimationsModule,
        HttpClientModule,
        StoreModule.forRoot(reducers, {}),
        LocalStorageModule.forRoot({
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
        SharedModule.forRoot(),
        AppRoutingModule
    ],
    providers: [
        httpInterceptorProviders,
        { provide: APP_BASE_HREF, useValue: window.document.querySelector('base').getAttribute('href') },
        { provide: COMPOSITION_BUFFER_MODE, useValue: false },
        { provide: SERVER_MAP_TYPE, useValue: ServerMapType.CYTOSCAPEJS }
    ],
    bootstrap: [AppComponent]
})
export class AppModule {}
