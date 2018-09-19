import { Routes } from '@angular/router';

import { UrlPath, UrlPathId } from 'app/shared/models';
import { TransactionDetailContentsContainerComponent } from 'app/core/components/transaction-detail-contents/transaction-detail-contents-container.component';
import { SystemConfigurationResolverService, ApplicationListResolverService } from 'app/shared/services';
import { TransactionDetailPageComponent } from './transaction-detail-page.component';

const TO_MAIN = '/' + UrlPath.MAIN;

export const routing: Routes = [
    {
        path: '',
        component: TransactionDetailPageComponent,
        resolve: {
            configuration: SystemConfigurationResolverService,
            applicationList: ApplicationListResolverService
        },
        children: [
            {
                path: '',
                redirectTo: TO_MAIN,
                pathMatch: 'full'
            },
            {
                path: ':' + UrlPathId.TRACE_ID,
                redirectTo: TO_MAIN,
                pathMatch: 'full'
            },
            {
                path: ':' + UrlPathId.TRACE_ID + '/:' + UrlPathId.FOCUS_TIMESTAMP,
                redirectTo: TO_MAIN,
                pathMatch: 'full'
            },
            {
                path: ':' + UrlPathId.TRACE_ID + '/:' + UrlPathId.FOCUS_TIMESTAMP + '/:' + UrlPathId.AGENT_ID,
                redirectTo: TO_MAIN,
                pathMatch: 'full'
            },
            {
                path: ':' + UrlPathId.TRACE_ID + '/:' + UrlPathId.FOCUS_TIMESTAMP + '/:' + UrlPathId.AGENT_ID + '/:' + UrlPathId.SPAN_ID,
                children: [
                    {
                        path: '',
                        component: TransactionDetailContentsContainerComponent
                    },
                    {
                        path: ':' + UrlPathId.VIEW_TYPE,
                        children: [
                            {
                                path: '',
                                component: TransactionDetailContentsContainerComponent,
                            },
                            {
                                path: ':' + UrlPathId.SEARCH_ID,
                                children: [
                                    {
                                        path: '',
                                        component: TransactionDetailContentsContainerComponent,
                                    }
                                ]
                            }
                        ]
                    }
                ]
            }
        ]
    }
];
