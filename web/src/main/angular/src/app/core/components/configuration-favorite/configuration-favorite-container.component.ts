import { Component, OnInit, ChangeDetectionStrategy } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { Observable } from 'rxjs';

@Component({
    selector: 'pp-configuration-favorite-container',
    templateUrl: './configuration-favorite-container.component.html',
    styleUrls: ['./configuration-favorite-container.component.css'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class ConfigurationFavoriteContainerComponent implements OnInit {
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
