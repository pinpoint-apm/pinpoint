import { Routes } from '@angular/router';

import { UrlPath, UrlPathId } from 'app/shared/models';
import { ApplicationInspectorContentsContainerComponent } from 'app/core/components/application-inspector-contents/application-inspector-contents-container.component';
import { AgentInspectorContentsContainerComponent } from 'app/core/components/agent-inspector-contents/agent-inspector-contents-container.component';
import { UrlRedirectorComponent } from 'app/shared/components/url-redirector/url-redirector.component';
import { InspectorPageComponent } from './inspector-page.component';
import { ServerTimeResolverService } from 'app/shared/services';
import { EmptyContentsComponent } from 'app/shared/components/empty-contents';

export const routing: Routes = [
    {
        path: '',
        component: InspectorPageComponent,
        children: [
            {
                path: UrlPath.REAL_TIME,
                children: [
                    {
                        path: '',
                        pathMatch: 'full',
                        redirectTo: '/' + UrlPath.INSPECTOR
                    },
                    {
                        path: ':' + UrlPathId.APPLICATION,
                        // resolve: {
                        //     serverTime: ServerTimeResolverService
                        // },
                        data: {
                            showRealTimeButton: true,
                            enableRealTimeMode: true
                        },
                        children: [
                            {
                                path: '',
                                pathMatch: 'full',
                                resolve: {
                                    serverTime: ServerTimeResolverService
                                },
                                component: ApplicationInspectorContentsContainerComponent
                            },
                            {
                                path: ':' + UrlPathId.AGENT_ID,
                                resolve: {
                                    serverTime: ServerTimeResolverService
                                },
                                component: AgentInspectorContentsContainerComponent
                            }
                        ]
                    }
                ]
            },
            {
                path: ':' + UrlPathId.APPLICATION,
                children: [
                    {
                        path: '',
                        pathMatch: 'full',
                        data: {
                            path: UrlPath.INSPECTOR
                        },
                        component: UrlRedirectorComponent
                    },
                    {
                        path: ':' + UrlPathId.PERIOD,
                        children: [
                            {
                                path: '',
                                pathMatch: 'full',
                                data: {
                                    path: UrlPath.INSPECTOR
                                },
                                component: UrlRedirectorComponent
                            },
                            {
                                path: ':' + UrlPathId.END_TIME,
                                data: {
                                    showRealTimeButton: true,
                                    enableRealTimeMode: false
                                },
                                children: [
                                    {
                                        path: '',
                                        pathMatch: 'full',
                                        component: ApplicationInspectorContentsContainerComponent
                                    },
                                    {
                                        path: ':' + UrlPathId.AGENT_ID,
                                        component: AgentInspectorContentsContainerComponent
                                    }
                                ]
                            }
                        ]
                    }
                ]
            },
            {
                path: '',
                pathMatch: 'full',
                component: EmptyContentsComponent
            }
        ]
    }
];
