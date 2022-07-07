import { Component, OnInit, OnDestroy, ComponentFactoryResolver, Injector } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { Subject, forkJoin, combineLatest } from 'rxjs';
import { filter, map, skipWhile, takeUntil } from 'rxjs/operators';

import { WebAppSettingDataService, StoreHelperService, AnalyticsService, TRACKED_EVENT_LIST, DynamicPopupService } from 'app/shared/services';
import { isEmpty } from 'app/core/utils/util';
import { initFavoriteApplicationList, removeFavApplication } from 'app/shared/store/actions';
import { ServerErrorPopupContainerComponent } from 'app/core/components/server-error-popup/server-error-popup-container.component';

@Component({
    selector: 'pp-favorite-application-list-for-configuration-container',
    templateUrl: './application-list-for-configuration-container.component.html',
    styleUrls: ['./application-list-for-configuration-container.component.css']
})
export class FavoriteApplicationListForConfigurationContainerComponent implements OnInit, OnDestroy {
    private unsubscribe = new Subject<void>();
    private _query = '';
    private favoriteAppList: IApplication[] = [];

    filteredAppList: IApplication[];
    funcImagePath: Function;
    iconBtnClassName = 'far fa-trash-alt';
    searchUseEnter = false;
    SEARCH_MIN_LENGTH = 2;
    i18nText = {
        SEARCH_INPUT_GUIDE: '',
        EMPTY: ''
    };
    isEmpty: boolean;
    useDisable = true;
    showLoading = true;

    constructor(
        private storeHelperService: StoreHelperService,
        private translateService: TranslateService,
        private webAppSettingDataService: WebAppSettingDataService,
        private analyticsService: AnalyticsService,
        private dynamicPopupService: DynamicPopupService,
        private componentFactoryResolver: ComponentFactoryResolver,
        private injector: Injector
    ) {}

    ngOnInit() {
        this.initList();
        this.initI18nText();
        this.connectStore();
        this.funcImagePath = this.webAppSettingDataService.getIconPathMakeFunc();
    }

    ngOnDestroy() {
        this.unsubscribe.next();
        this.unsubscribe.complete();
    }

    private initList(): void {
        this.showProcessing();
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

    private connectStore(): void {
        combineLatest(
            this.storeHelperService.getApplicationList(this.unsubscribe).pipe(
                skipWhile((list: IApplication[]) => list === null)
            ),
            this.storeHelperService.getFavoriteApplicationList(this.unsubscribe).pipe(
                skipWhile((list: IApplication[]) => list === null)
            )
        ).pipe(
            map(([appList, favAppList]: IApplication[][]) => {
                const validFavAppList = favAppList.filter((favApp: IApplication) => {
                    return appList.some((app: IApplication) => app.equals(favApp));
                });

                return validFavAppList;
            }),
            takeUntil(this.unsubscribe)
        ).subscribe((favAppList: IApplication[]) => {
            this.hideProcessing();
            this.favoriteAppList = favAppList;
            this.filterList();
        });

        this.storeHelperService.getApplicationListError().pipe(
            takeUntil(this.unsubscribe)
        ).subscribe((error: IServerError) => {
            this.hideProcessing();
        });

        this.storeHelperService.getFavoriteApplicationListError().pipe(
            takeUntil(this.unsubscribe)
        ).subscribe((error: IServerError) => {
            this.hideProcessing();
        });

        this.storeHelperService.getFavoriteApplicationRemoveError().pipe(
            takeUntil(this.unsubscribe)
        ).subscribe((error: IServerError) => {
            this.hideProcessing();
            this.dynamicPopupService.openPopup({
                data: {
                    title: 'Server Error',
                    contents: error
                },
                component: ServerErrorPopupContainerComponent,
                onCloseCallback: () => {
                    this.storeHelperService.dispatch(initFavoriteApplicationList());
                }
            }, {
                resolver: this.componentFactoryResolver,
                injector: this.injector
            });
        });
    }

    private filterList(): void {
        if (this.query === '') {
            this.filteredAppList = this.favoriteAppList;
        } else {
            this.filteredAppList = this.favoriteAppList.filter((app: IApplication) => {
                return new RegExp(this.query, 'i').test(app.getApplicationName());
            });
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
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.REMOVE_FAVORITE_APPLICATION);
        this.storeHelperService.dispatch(removeFavApplication(app));
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
