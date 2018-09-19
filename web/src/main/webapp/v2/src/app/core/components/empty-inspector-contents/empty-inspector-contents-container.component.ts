import { Component, OnInit } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';

import { Observable } from 'rxjs';

@Component({
    selector: 'pp-empty-inspector-contents-container',
    templateUrl: './empty-inspector-contents-container.component.html',
    styleUrls: ['./empty-inspector-contents-container.component.css']
})
export class EmptyInspectorContentsContainerComponent implements OnInit {
    guideText$: Observable<string>;

    constructor(
        private translateService: TranslateService,
    ) { }

    ngOnInit() {
        this.guideText$ = this.translateService.get('MAIN.SELECT_YOUR_APP');
    }
}
