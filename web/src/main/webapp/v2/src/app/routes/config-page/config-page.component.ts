import { Component, OnInit } from '@angular/core';
import { take, map } from 'rxjs/operators';

import { RouteInfoCollectorService, UrlRouteManagerService, StoreHelperService } from 'app/shared/services';

@Component({
    selector: 'pp-config-page',
    templateUrl: './config-page.component.html',
    styleUrls: ['./config-page.component.css']
})
export class ConfigPageComponent implements OnInit {
    constructor(
        private routeInfoCollectorService: RouteInfoCollectorService,
        private urlRouteManagerService: UrlRouteManagerService,
        private storeHelperService: StoreHelperService
    ) {}

    ngOnInit() {}
    onClickExit(): void {
        this.storeHelperService.getURLPath().pipe(
            take(1),
            map((urlPath: string) => {
                return urlPath.split('/').slice(1).map((path: string) => decodeURIComponent(path));
            })
        ).subscribe((url: string[]) => {
            this.urlRouteManagerService.moveOnPage({ url });
        });
    }
}
