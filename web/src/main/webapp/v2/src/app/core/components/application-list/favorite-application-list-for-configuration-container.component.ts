import { Component, OnInit, OnDestroy, ChangeDetectionStrategy } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { Observable, Subject } from 'rxjs';

import { WebAppSettingDataService, StoreHelperService } from 'app/shared/services';

@Component({
    selector: 'pp-favorite-application-list-for-configuration-container',
    templateUrl: './application-list-for-configuration-container.component.html',
    styleUrls: ['./application-list-for-configuration-container.component.css'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class FavoriteApplicationListForConfigurationContainerComponent implements OnInit, OnDestroy {
    private unsubscribe: Subject<void> = new Subject();
    applicationList$: Observable<IApplication[]>;
    emptyText$: Observable<string>;
    funcImagePath: Function;
    iconBtnClassName = 'far fa-trash-alt';

    constructor(
        private storeHelperService: StoreHelperService,
        private translateService: TranslateService,
        private webAppSettingDataService: WebAppSettingDataService,
    ) {}

    ngOnInit() {
        this.initList();
        this.funcImagePath = this.webAppSettingDataService.getIconPathMakeFunc();
        this.initEmptyText();
    }
    ngOnDestroy() {
        this.unsubscribe.next();
        this.unsubscribe.complete();
    }

    private initList(): void {
        this.applicationList$ = this.storeHelperService.getFavoriteApplicationList(this.unsubscribe);
    }
    private initEmptyText(): void {
        this.emptyText$ = this.translateService.get('CONFIGURATION.GENERAL.EMPTY');
    }

    onSelectApp(app: IApplication): void {
        this.webAppSettingDataService.removeFavoriteApplication(app);
    }
}
