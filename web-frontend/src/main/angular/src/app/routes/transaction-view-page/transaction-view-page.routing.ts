import { Routes } from '@angular/router';

import { TransactionViewTopContentsContainerComponent } from 'app/core/components/transaction-view-top-contents/transaction-view-top-contents-container.component';
import { TransactionViewPageComponent } from './transaction-view-page.component';

export const routing: Routes = [
    {
        path: '',
        component: TransactionViewPageComponent,
        children: [
            {
                path: '',
                component: TransactionViewTopContentsContainerComponent,
                pathMatch: 'full'
            }
        ]
    }
];
