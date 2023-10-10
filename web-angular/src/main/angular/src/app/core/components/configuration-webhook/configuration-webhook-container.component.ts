import {Component, OnInit} from '@angular/core';
import {Observable} from 'rxjs';
import {tap} from 'rxjs/operators';

import {UrlRouteManagerService, WebAppSettingDataService} from 'app/shared/services';

@Component({
    selector: 'pp-configuration-webhook',
    templateUrl: './configuration-webhook-container.component.html',
    styleUrls: ['./configuration-webhook-container.component.css']
})
export class ConfigurationWebhookContainerComponent implements OnInit {
    webhookEnable$: Observable<boolean>;

    constructor(
        private webAppSettingDataService: WebAppSettingDataService,
        private urlRouteManagerService: UrlRouteManagerService
    ) {
    }

    ngOnInit() {
        this.webhookEnable$ = this.webAppSettingDataService.isWebhookEnable().pipe(
            tap((webhookEnable: boolean) => {
                if (!webhookEnable) {
                    this.urlRouteManagerService.moveToMain();
                }
            })
        );
    }
}
