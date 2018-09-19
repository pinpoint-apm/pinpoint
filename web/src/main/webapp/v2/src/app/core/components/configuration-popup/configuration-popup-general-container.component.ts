import { Component, OnInit, ChangeDetectionStrategy } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { Observable } from 'rxjs';

@Component({
    selector: 'pp-configuration-popup-general-container',
    templateUrl: './configuration-popup-general-container.component.html',
    styleUrls: ['./configuration-popup-general-container.component.css'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class ConfigurationPopupGeneralContainerComponent implements OnInit {
    desc$: Observable<string>;

    constructor(
        private translateService: TranslateService,
    ) {}
    ngOnInit() {
        this.initDescText();
    }

    private initDescText(): void {
        this.desc$ = this.translateService.get('CONFIGURATION.GENERAL.DESC');
    }
}
