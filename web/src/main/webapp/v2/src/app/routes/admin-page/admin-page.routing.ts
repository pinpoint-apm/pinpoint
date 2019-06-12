import { Routes } from '@angular/router';

import { UrlPathId } from 'app/shared/models';
import { ServerTimeResolverService } from 'app/shared/services';
import { AgentStatContentsContainerComponent } from 'app/core/components/agent-stat-contents/agent-stat-contents-container.component';
import { AgentManagementContentsContainerComponent } from 'app/core/components/agent-management-contents/agent-management-contents-container.component';
import { AdminPageComponent } from './admin-page.component';

export const routing: Routes = [
    {
        path: '',
        component: AdminPageComponent,
        resolve: {
            serverTime: ServerTimeResolverService
        },
        children: [
            {
                path: UrlPathId.AGENT,
                component: AgentManagementContentsContainerComponent
            },
            {
                path: UrlPathId.STAT,
                component: AgentStatContentsContainerComponent
            }
        ]
    }
];
