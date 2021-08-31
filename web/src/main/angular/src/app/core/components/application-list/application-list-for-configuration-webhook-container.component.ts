import { Component, OnInit, OnDestroy, ComponentFactoryResolver, Injector } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { Subject, forkJoin } from 'rxjs';
import { filter, takeUntil } from 'rxjs/operators';

import { WebAppSettingDataService, StoreHelperService, AnalyticsService, TRACKED_EVENT_LIST, DynamicPopupService } from 'app/shared/services';
import { ApplicationListInteractionForConfigurationService } from './application-list-interaction-for-configuration.service';
import { FOCUS_TYPE } from './application-list-for-header.component';
import { isEmpty } from 'app/core/utils/util';
import { getApplicationList, initApplicationList } from 'app/shared/store/actions';
import { ServerErrorPopupContainerComponent } from 'app/core/components/server-error-popup/server-error-popup-container.component';

@Component({
    selector: 'pp-application-list-for-configuration-webhook-container',
    templateUrl: './application-list-for-configuration-webhook-container.component.html',
    styleUrls: ['./application-list-for-configuration-webhook-container.component.css'],
})
export class ApplicationListForConfigurationWebhookContainerComponent implements OnInit, OnDestroy {
    private unsubscribe = new Subject<void>();
    private _query = '';
    private originalAppList: IApplication[] = [];

    filteredAppList: IApplication[];
    funcImagePath: Function;
    selectedApp: IApplication;
    showTitle = false;
    focusType: FOCUS_TYPE = FOCUS_TYPE.KEYBOARD;
    restCount = 0;
    focusIndex = -1;
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
        private webAppSettingDataService: WebAppSettingDataService,
        private translateService: TranslateService,
        private applicationListInteractionForConfigurationService: ApplicationListInteractionForConfigurationService,
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
        this.storeHelperService.dispatch(getApplicationList());
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
        this.storeHelperService.getApplicationList(this.unsubscribe).pipe(
            filter((appList: IApplication[]) => !isEmpty(appList)),
            takeUntil(this.unsubscribe)
        ).subscribe((appList: IApplication[]) => {
            this.hideProcessing();
            this.originalAppList = appList;
            this.filterList();
        });

        this.storeHelperService.getApplicationListError().pipe(
            takeUntil(this.unsubscribe)
        ).subscribe((error: IServerErrorFormat) => {
            this.hideProcessing();
            this.dynamicPopupService.openPopup({
                data: {
                    title: 'Server Error',
                    contents: error
                },
                component: ServerErrorPopupContainerComponent,
                onCloseCallback: () => {
                    this.storeHelperService.dispatch(initApplicationList());
                }
            }, {
                resolver: this.componentFactoryResolver,
                injector: this.injector
            });
        });
    }

    private filterList(): void {
        if (this.query === '') {
            this.filteredAppList = this.originalAppList;
        } else {
            this.filteredAppList = this.originalAppList.filter((app: IApplication) => {
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
        if (app.equals(this.selectedApp)) {
            return;
        }

        this.selectedApp = app;
        this.applicationListInteractionForConfigurationService.setSelectedApplication(app);
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.SELECT_APPLICATION_FOR_WEBHOOK);
    }

    onFocused(index: number): void {
        this.focusIndex = index;
        this.focusType = FOCUS_TYPE.MOUSE;
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
