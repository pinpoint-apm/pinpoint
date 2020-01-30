import { Routes } from '@angular/router';

import { UrlPath, UrlPathId } from 'app/shared/models';
import { FilteredMapContentsContainerComponent } from 'app/core/components/filtered-map-contents/filtered-map-contents-container.component';
import { UrlRedirectorComponent } from 'app/shared/components/url-redirector/url-redirector.component';
import { FilteredMapPageComponent } from './filtered-map-page.component';

export const routing: Routes = [
    {
        path: '',
        component: FilteredMapPageComponent,
        children: [
            {
                path: ':' + UrlPathId.APPLICATION + '/:' + UrlPathId.PERIOD + '/:' + UrlPathId.END_TIME,
                component: FilteredMapContentsContainerComponent
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
                redirectTo: `/${UrlPath.MAIN}`,
                pathMatch: 'full'
            }
        ]
    }
];
