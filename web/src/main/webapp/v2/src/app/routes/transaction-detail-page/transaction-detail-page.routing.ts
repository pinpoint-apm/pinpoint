import { Routes } from '@angular/router';

import { UrlPath, UrlPathId } from 'app/shared/models';
import { TransactionDetailContentsContainerComponent } from 'app/core/components/transaction-detail-contents/transaction-detail-contents-container.component';
import { TransactionDetailPageComponent } from './transaction-detail-page.component';

const TO_MAIN = `/${UrlPath.MAIN}`;

export const routing: Routes = [
    {
        path: '',
        component: TransactionDetailPageComponent,
        children: [
            {
                path: ':' + UrlPathId.TRACE_ID + '/:' + UrlPathId.FOCUS_TIMESTAMP + '/:' + UrlPathId.AGENT_ID + '/:' + UrlPathId.SPAN_ID,
                component: TransactionDetailContentsContainerComponent,
            },
            {
                path: ':' + UrlPathId.TRACE_ID + '/:' + UrlPathId.FOCUS_TIMESTAMP + '/:' + UrlPathId.AGENT_ID,
                redirectTo: TO_MAIN,
                pathMatch: 'full'
            },
            {
                path: ':' + UrlPathId.TRACE_ID + '/:' + UrlPathId.FOCUS_TIMESTAMP,
                redirectTo: TO_MAIN,
                pathMatch: 'full'
            },
            {
                path: ':' + UrlPathId.TRACE_ID,
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
