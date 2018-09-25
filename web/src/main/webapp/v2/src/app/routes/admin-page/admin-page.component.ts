import { Component, OnInit, OnDestroy } from '@angular/core';

import { RouteInfoCollectorService } from 'app/shared/services';

@Component({
    selector: 'pp-admin-page',
    templateUrl: './admin-page.component.html',
    styleUrls: ['./admin-page.component.css']
})
export class AdminPageComponent implements OnInit, OnDestroy {
    constructor(private routeInfoCollectorService: RouteInfoCollectorService) {}
    ngOnInit() {}
    ngOnDestroy() {}
}
