import { Component, OnInit } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { Observable } from 'rxjs';

@Component({
    selector: 'pp-application-inspector-usage-guide-container',
    templateUrl: './application-inspector-usage-guide-container.component.html',
    styleUrls: ['./application-inspector-usage-guide-container.component.css']
})
export class ApplicationInspectorUsageGuideContainerComponent implements OnInit {
    guideMessage$: Observable<string>;

    constructor(
        private translateService: TranslateService
    ) {}

    ngOnInit() {
        this.guideMessage$ = this.translateService.get('INSPECTOR.APPLICATION_INSPECTOR_USAGE_GUIDE_MESSAGE');
    }
}
