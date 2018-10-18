import { Routes } from '@angular/router';

import { UrlPath, UrlPathId } from 'app/shared/models';
import { TransactionListEmptyComponent } from './transaction-list-empty.component';
import { TransactionListBottomContentsContainerComponent } from 'app/core/components/transaction-list-bottom-contents/transaction-list-bottom-contents-container.component';
import { SystemConfigurationResolverService, ApplicationListResolverService } from 'app/shared/services';
import { TransactionListPageComponent } from './transaction-list-page.component';

const TO_MAIN = '/' + UrlPath.MAIN;

export const routing: Routes = [
    {
        path: '',
        component: TransactionListPageComponent,
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
                path: ':' + UrlPathId.APPLICATION,
                redirectTo: TO_MAIN,
                pathMatch: 'full'
            },
            {
                path: ':' + UrlPathId.APPLICATION + '/:' + UrlPathId.PERIOD,
                redirectTo: TO_MAIN,
                pathMatch: 'full'
            },
            {
                path: ':' + UrlPathId.APPLICATION + '/:' + UrlPathId.PERIOD + '/:' + UrlPathId.END_TIME,
                children: [
                    {
                        path: '',
                        component: TransactionListEmptyComponent
                    },
                    {
                        path: ':' + UrlPathId.TRANSACTION_INFO,
                        children: [
                            {
                                path: '',
                                component: TransactionListBottomContentsContainerComponent,
                            },
                            {
                                path: ':' + UrlPathId.VIEW_TYPE,
                                children: [
                                    {
                                        path: '',
                                        component: TransactionListBottomContentsContainerComponent,
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
