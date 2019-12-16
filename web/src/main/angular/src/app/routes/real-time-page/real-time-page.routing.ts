import { Routes } from '@angular/router';

import { UrlPath, UrlPathId } from 'app/shared/models';
import { RealTimePagingContainerComponent } from 'app/core/components/real-time/real-time-paging-container.component';
import { ServerTimeResolverService } from 'app/shared/services';
import { RealTimePageComponent } from './real-time-page.component';

const TO_MAIN = `/${UrlPath.MAIN}`;

export const routing: Routes = [
    {
        path: '',
        component: RealTimePageComponent,
        children: [
            {
                path: ':' + UrlPathId.APPLICATION + '/:' + UrlPathId.PAGE,
                resolve: {
                    serverTime: ServerTimeResolverService
                },
                component: RealTimePagingContainerComponent
            },
            {
                path: ':' + UrlPathId.APPLICATION,
                redirectTo: TO_MAIN,
                pathMatch: 'full'
            },
            {
                path: '',
                redirectTo: TO_MAIN,
                pathMatch: 'full'
            }
        ]
    }
];
