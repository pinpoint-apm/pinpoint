import { Routes } from '@angular/router';

import { TransactionDetailContentsContainerComponent } from 'app/core/components/transaction-detail-contents/transaction-detail-contents-container.component';
import { TransactionDetailPageComponent } from './transaction-detail-page.component';

export const routing: Routes = [
    {
        path: '',
        component: TransactionDetailPageComponent,
        children: [
            {
                path: '',
                component: TransactionDetailContentsContainerComponent,
                pathMatch: 'full'
            }
        ]
    }
];
