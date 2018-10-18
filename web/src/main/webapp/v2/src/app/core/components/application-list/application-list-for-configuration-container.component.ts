import { Component, OnInit, OnDestroy, ChangeDetectionStrategy } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { Observable, combineLatest, Subject } from 'rxjs';
import { map } from 'rxjs/operators';

import { StoreHelperService, WebAppSettingDataService, AnalyticsService, TRACKED_EVENT_LIST } from 'app/shared/services';

@Component({
    selector: 'pp-application-list-for-configuration-container',
    templateUrl: './application-list-for-configuration-container.component.html',
    styleUrls: ['./application-list-for-configuration-container.component.css'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class ApplicationListForConfigurationContainerComponent implements OnInit, OnDestroy {
    private unsubscribe: Subject<void> = new Subject();
    applicationList$: Observable<IApplication[]>;
    funcImagePath: Function;
    emptyText$: Observable<string>;
    iconBtnClassName = 'fas fa-arrow-right';

    constructor(
        private storeHelperService: StoreHelperService,
        private webAppSettingDataService: WebAppSettingDataService,
        private translateService: TranslateService,
        private analyticsService: AnalyticsService,
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
        this.applicationList$ = combineLatest(
            this.storeHelperService.getApplicationList(this.unsubscribe),
            this.storeHelperService.getFavoriteApplicationList(this.unsubscribe),
        ).pipe(
            map(([appList, favAppList]: IApplication[][]) => {
                return appList.filter((app: IApplication) => {
                    return favAppList.findIndex((favApp: IApplication) => {
                        return favApp.equals(app);
                    }) === -1;
                });
            })
        );
    }
    private initEmptyText(): void {
        this.emptyText$ = this.translateService.get('CONFIGURATION.GENERAL.EMPTY');
    }

    onSelectApp(app: IApplication): void {
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.SET_FAVORITE_APPLICATION_IN_CONFIGURATION);
        this.webAppSettingDataService.addFavoriteApplication(app);
    }
}
