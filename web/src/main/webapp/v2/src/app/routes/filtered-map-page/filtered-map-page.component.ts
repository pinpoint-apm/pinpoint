import { Component, OnInit } from '@angular/core';

import { RouteInfoCollectorService } from 'app/shared/services';

@Component({
    selector: 'pp-filtered-map-page',
    templateUrl: './filtered-map-page.component.html',
    styleUrls: ['./filtered-map-page.component.css']
})
export class FilteredMapPageComponent implements OnInit {
    constructor(private routeInfoCollectorService: RouteInfoCollectorService) {}
    ngOnInit() {}
}
