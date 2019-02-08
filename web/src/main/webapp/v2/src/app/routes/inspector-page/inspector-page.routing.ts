import { Routes } from '@angular/router';
import { UrlPath, UrlPathId } from 'app/shared/models';
import { ApplicationInspectorContentsContainerComponent } from 'app/core/components/application-inspector-contents/application-inspector-contents-container.component';
import { AgentInspectorContentsContainerComponent } from 'app/core/components/agent-inspector-contents/agent-inspector-contents-container.component';
import { EmptyInspectorContentsContainerComponent } from 'app/core/components/empty-inspector-contents/empty-inspector-contents-container.component';
import { UrlRedirectorComponent } from 'app/shared/components/url-redirector/url-redirector.component';
import { SystemConfigurationResolverService, ApplicationListResolverService } from 'app/shared/services';
import { InspectorPageComponent } from './inspector-page.component';

export const routing: Routes = [
    {
        path: '',
        component: InspectorPageComponent,
        resolve: {
            configuration: SystemConfigurationResolverService,
            applicationList: ApplicationListResolverService
        },
        children: [
            {
                path: ':' + UrlPathId.APPLICATION + '/:' + UrlPathId.PERIOD + '/:' + UrlPathId.END_TIME + '/:' + UrlPathId.AGENT_ID,
                data: {
                    showRealTimeButton: false,
                    enableRealTimeMode: false
                },
                component: AgentInspectorContentsContainerComponent
            },
            {
                path: ':' + UrlPathId.APPLICATION + '/:' + UrlPathId.PERIOD + '/:' + UrlPathId.END_TIME,
                data: {
                    showRealTimeButton: false,
                    enableRealTimeMode: false
                },
                component: ApplicationInspectorContentsContainerComponent
            },
            {
                path: ':' + UrlPathId.APPLICATION + '/:' + UrlPathId.PERIOD,
                data: {
                    path: UrlPath.INSPECTOR
                },
                component: UrlRedirectorComponent
            },
            {
                path: ':' + UrlPathId.APPLICATION,
                data: {
                    path: UrlPath.INSPECTOR
                },
                component: UrlRedirectorComponent
            },
            {
                path: '',
                component: EmptyInspectorContentsContainerComponent
            }
        ]
    }
];
