import { Component, OnInit } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { Observable } from 'rxjs';

import { StoreHelperService } from 'app/shared/services';
import { getApplicationList, getFavApplicationList } from 'app/shared/store/actions';

@Component({
    selector: 'pp-configuration-favorite-container',
    templateUrl: './configuration-favorite-container.component.html',
    styleUrls: ['./configuration-favorite-container.component.css'],
})
export class ConfigurationFavoriteContainerComponent implements OnInit {
    desc$: Observable<string>;

    constructor(
        private translateService: TranslateService,
        private storeHelperService: StoreHelperService,
    ) {}

    ngOnInit() {
        this.initDescText();
        this.storeHelperService.dispatch(getApplicationList());
        this.storeHelperService.dispatch(getFavApplicationList());
    }

    private initDescText(): void {
        this.desc$ = this.translateService.get('CONFIGURATION.GENERAL.DESC');
    }
}
