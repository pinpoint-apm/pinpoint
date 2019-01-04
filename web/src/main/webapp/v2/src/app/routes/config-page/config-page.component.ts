import { Component, OnInit } from '@angular/core';

import { UrlRouteManagerService } from 'app/shared/services';

@Component({
    selector: 'pp-config-page',
    templateUrl: './config-page.component.html',
    styleUrls: ['./config-page.component.css']
})
export class ConfigPageComponent implements OnInit {
    constructor(
        private urlRouteManagerService: UrlRouteManagerService,
    ) {}
    ngOnInit() {}
    onMoveBack(): void {
        this.urlRouteManagerService.back();
    }
}
