import { Component, OnInit } from '@angular/core';
import { Observable } from 'rxjs';

import { WebAppSettingDataService } from 'app/shared/services';

@Component({
    selector: 'pp-application-inspector-contents-container',
    templateUrl: './application-inspector-contents-container.component.html',
    styleUrls: ['./application-inspector-contents-container.component.css']
})
export class ApplicationInspectorContentsContainerComponent implements OnInit {
    isApplicationInspectorActivated$: Observable<boolean>;

    constructor(
        private webAppSettingDataService: WebAppSettingDataService
    ) {}

    ngOnInit() {
        this.isApplicationInspectorActivated$ = this.webAppSettingDataService.isApplicationInspectorActivated();
    }
}
