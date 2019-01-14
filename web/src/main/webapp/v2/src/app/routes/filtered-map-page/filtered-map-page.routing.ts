import { Routes } from '@angular/router';

import { UrlPath, UrlPathId } from 'app/shared/models';
import { FilteredMapContentsContainerComponent } from 'app/core/components/filtered-map-contents/filtered-map-contents-container.component';
// import { NoneComponent } from 'app/shared/components/empty-contents';
import { SystemConfigurationResolverService, ApplicationListResolverService } from 'app/shared/services';
// import { UrlValidateGuard } from 'app/shared/services';
import { UrlRedirectorComponent } from 'app/shared/components/url-redirector/url-redirector.component';
import { FilteredMapPageComponent } from './filtered-map-page.component';

export const routing: Routes = [
    {
        path: '',
        component: FilteredMapPageComponent,
        resolve: {
            configuration: SystemConfigurationResolverService,
            applicationList: ApplicationListResolverService
        },
        children: [
            {
                path: '',
                redirectTo: '/' + UrlPath.MAIN,
                pathMatch: 'full'
            },
            {
                path: ':' + UrlPathId.APPLICATION,
                data: {
                    path: UrlPath.MAIN
                },
                component: UrlRedirectorComponent
            },
            {
                path: ':' + UrlPathId.APPLICATION + '/:' + UrlPathId.PERIOD,
                data: {
                    path: UrlPath.MAIN
                },
                component: UrlRedirectorComponent
            },
            {
                path: ':' + UrlPathId.APPLICATION + '/:' + UrlPathId.PERIOD + '/:' + UrlPathId.END_TIME,
                children: [
                    {
                        path: '',
                        component: FilteredMapContentsContainerComponent
                    },
                    {
                        path: ':' + UrlPathId.FILTER,
                        children: [
                            {
                                path: '',
                                component: FilteredMapContentsContainerComponent
                            },
                            {
                                path: ':' + UrlPathId.HINT,
                                children: [
                                    {
                                        path: '',
                                        component: FilteredMapContentsContainerComponent
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

// export const routing: Routes = [
//     {
//         path: UrlPath.FILTERED_MAP,
//         component: FilteredMapPageComponent,
//         canActivate: [ UrlValidateGuard ],
//         resolve: {
//             configuration: SystemConfigurationResolverService,
//             applicationList: ApplicationListResolverService
//         },
//         children: [
//             {
//                 path: '',
//                 component: NoneComponent
//             },
//             {
//                 path: ':' + UrlPathId.APPLICATION,
//                 component: NoneComponent
//             },
//             {
//                 path: ':' + UrlPathId.APPLICATION + '/:' + UrlPathId.PERIOD,
//                 component: NoneComponent
//             },
//             {
//                 path: ':' + UrlPathId.APPLICATION + '/:' + UrlPathId.PERIOD + '/:' + UrlPathId.END_TIME,
//                 children: [
//                     {
//                         path: '',
//                         component: FilteredMapContentsContainerComponent
//                     },
//                     {
//                         path: '',
//                         component: SideBarForFilteredMapContainerComponent,
//                         outlet: 'sidebar'
//                     }
//                 ]
//             },
//             {
//                 path: ':' + UrlPathId.APPLICATION + '/:' + UrlPathId.PERIOD + '/:' + UrlPathId.END_TIME + '/:' + UrlPathId.FILTER,
//                 children: [
//                     {
//                         path: '',
//                         component: FilteredMapContentsContainerComponent
//                     },
//                     {
//                         path: '',
//                         component: SideBarForFilteredMapContainerComponent,
//                         outlet: 'sidebar'
//                     }
//                 ]
//             },
//             {
//                 path: ':' + UrlPathId.APPLICATION + '/:' + UrlPathId.PERIOD + '/:' + UrlPathId.END_TIME + '/:' + UrlPathId.FILTER + '/:' + UrlPathId.HINT,
//                 children: [
//                     {
//                         path: '',
//                         component: FilteredMapContentsContainerComponent
//                     },
//                     {
//                         path: '',
//                         component: SideBarForFilteredMapContainerComponent,
//                         outlet: 'sidebar'
//                     }
//                 ]
//             }

//         ]
//     }
// ];
