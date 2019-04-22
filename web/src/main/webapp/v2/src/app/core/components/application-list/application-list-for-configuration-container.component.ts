import { Component, OnInit, OnDestroy} from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { Subject, forkJoin } from 'rxjs';

import { StoreHelperService, WebAppSettingDataService, TranslateReplaceService, AnalyticsService, TRACKED_EVENT_LIST } from 'app/shared/services';

@Component({
    selector: 'pp-application-list-for-configuration-container',
    templateUrl: './application-list-for-configuration-container.component.html',
    styleUrls: ['./application-list-for-configuration-container.component.css']
})
export class ApplicationListForConfigurationContainerComponent implements OnInit, OnDestroy {
    private unsubscribe: Subject<void> = new Subject();
    originalApplicationList: IApplication[];
    favoriteApplicationList: IApplication[];
    applicationList: IApplication[];
    funcImagePath: Function;
    emptyText: string;
    iconBtnClassName = 'fas fa-arrow-right';
    query = '';
    searchUseEnter = false;
    SEARCH_MIN_LENGTH = 2;
    i18nText = {
        SEARCH_INPUT_GUIDE: ''
    };
    useDisable = true;
    showLoading = true;

    constructor(
        private storeHelperService: StoreHelperService,
        private webAppSettingDataService: WebAppSettingDataService,
        private translateService: TranslateService,
        private translateReplaceService: TranslateReplaceService,
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
        this.storeHelperService.getApplicationList(this.unsubscribe).subscribe((applicationList: IApplication[]) => {
            this.originalApplicationList = applicationList;
        });
        this.storeHelperService.getFavoriteApplicationList(this.unsubscribe).subscribe((favoriteApplicationList: IApplication[]) => {
            this.favoriteApplicationList = favoriteApplicationList;
            this.filterApplicationList();
        });
    }
    private filterApplicationList(): void {
        const applicationList = this.originalApplicationList.filter((app: IApplication) => {
            return this.favoriteApplicationList.findIndex((favApp: IApplication) => {
                return favApp.equals(app);
            }) === -1;
        });
        if (this.query !== '') {
            this.applicationList = applicationList.filter((app: IApplication) => {
                return app.getApplicationName().toLowerCase().indexOf(this.query.toLowerCase()) !== -1;
            });
        } else {
            this.applicationList = applicationList;
        }
        this.hideProcessing();
    }
    private initEmptyText(): void {
        forkJoin(
            this.translateService.get('COMMON.MIN_LENGTH'),
            this.translateService.get('COMMON.NO_CONTENT_TO_DISPLAY')
        ).subscribe(([minLengthMessage, emptyText]: string[]) => {
            this.i18nText.SEARCH_INPUT_GUIDE = this.translateReplaceService.replace(minLengthMessage, this.SEARCH_MIN_LENGTH);
            this.emptyText = emptyText;
        });
    }
    onClearSearch(input: HTMLInputElement): void {
        if (this.query !== '') {
            this.query = '';
            input.value = '';
            this.filterApplicationList();
            input.focus();
        }
    }
    onSearch(query: string): void {
        if (this.query !== query) {
            this.query = query;
            this.filterApplicationList();
        }
    }
    onSelectApp(app: IApplication): void {
        this.showProcessing();
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.SET_FAVORITE_APPLICATION_IN_CONFIGURATION);
        this.webAppSettingDataService.addFavoriteApplication(app);
    }
    private showProcessing(): void {
        this.useDisable = true;
        this.showLoading = true;
    }
    private hideProcessing(): void {
        this.useDisable = false;
        this.showLoading = false;
    }
}
