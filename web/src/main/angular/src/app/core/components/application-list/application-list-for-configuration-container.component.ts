import { Component, OnInit, OnDestroy} from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { Subject, forkJoin } from 'rxjs';

import { StoreHelperService, WebAppSettingDataService, AnalyticsService, TRACKED_EVENT_LIST } from 'app/shared/services';
import { isEmpty } from 'app/core/utils/util';

@Component({
    selector: 'pp-application-list-for-configuration-container',
    templateUrl: './application-list-for-configuration-container.component.html',
    styleUrls: ['./application-list-for-configuration-container.component.css']
})
export class ApplicationListForConfigurationContainerComponent implements OnInit, OnDestroy {
    private unsubscribe = new Subject<void>();
    private _query = '';
    private originalAppList: IApplication[];
    private favoriteAppList: IApplication[];

    filteredAppList: IApplication[];
    funcImagePath: Function;
    iconBtnClassName = 'fas fa-arrow-right';
    searchUseEnter = false;
    SEARCH_MIN_LENGTH = 2;
    i18nText = {
        SEARCH_INPUT_GUIDE: '',
        EMPTY: ''
    };
    isEmpty: boolean;

    constructor(
        private storeHelperService: StoreHelperService,
        private webAppSettingDataService: WebAppSettingDataService,
        private translateService: TranslateService,
        private analyticsService: AnalyticsService,
    ) {}

    ngOnInit() {
        this.initList();
        this.initI18nText();
        this.funcImagePath = this.webAppSettingDataService.getIconPathMakeFunc();
    }

    ngOnDestroy() {
        this.unsubscribe.next();
        this.unsubscribe.complete();
    }

    private initList(): void {
        this.storeHelperService.getApplicationList(this.unsubscribe).subscribe((appList: IApplication[]) => {
            this.originalAppList = appList;
        });
        this.storeHelperService.getFavoriteApplicationList(this.unsubscribe).subscribe((favoriteAppList: IApplication[]) => {
            this.favoriteAppList = favoriteAppList;
            this.filterList();
        });
    }

    private initI18nText(): void {
        forkJoin(
            this.translateService.get('COMMON.INPUT_APP_NAME_PLACE_HOLDER'),
            this.translateService.get('COMMON.EMPTY_ON_SEARCH')
        ).subscribe(([placeholderText, emptyText]: string[]) => {
            this.i18nText.SEARCH_INPUT_GUIDE = placeholderText;
            this.i18nText.EMPTY = emptyText;
        });
    }

    private filterList(): void {
        const appList = this.originalAppList.filter((app: IApplication) => {
            return this.favoriteAppList.findIndex((favApp: IApplication) => {
                return favApp.equals(app);
            }) === -1;
        });

        if (this.query !== '') {
            this.filteredAppList = appList.filter((app: IApplication) => {
                return app.getApplicationName().toLowerCase().indexOf(this.query.toLowerCase()) !== -1;
            });
        } else {
            this.filteredAppList = appList;
        }

        this.isEmpty = isEmpty(this.filteredAppList);
    }

    private set query(query: string) {
        this._query = query;
        this.filterList();
    }

    private get query(): string {
        return this._query;
    }

    onSearch(query: string): void {
        if (this.query === query) {
            return;
        }

        this.query = query;
    }

    onCancel(): void {
        this.query = '';
    }

    onSelectApp(app: IApplication): void {
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.SET_FAVORITE_APPLICATION_IN_CONFIGURATION);
        this.webAppSettingDataService.addFavoriteApplication(app);
    }
}
