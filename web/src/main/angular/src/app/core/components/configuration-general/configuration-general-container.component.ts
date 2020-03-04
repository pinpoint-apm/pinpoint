import { Component, OnInit, ChangeDetectionStrategy } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { Observable } from 'rxjs';

@Component({
    selector: 'pp-configuration-general-container',
    templateUrl: './configuration-general-container.component.html',
    styleUrls: ['./configuration-general-container.component.css'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class ConfigurationGeneralContainerComponent implements OnInit {
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
