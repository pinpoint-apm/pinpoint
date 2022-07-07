import { Component, OnInit, AfterViewInit, ViewChild, ElementRef, ChangeDetectorRef, ChangeDetectionStrategy, OnDestroy, Renderer2, ComponentFactoryResolver, Injector } from '@angular/core';
import { Subject, combineLatest, fromEvent, of, forkJoin } from 'rxjs';
import { debounceTime, distinctUntilChanged, filter, takeUntil, pluck, delay, map, skipWhile } from 'rxjs/operators';
import { TranslateService } from '@ngx-translate/core';

import {
    StoreHelperService,
    WebAppSettingDataService,
    UrlRouteManagerService,
    NewUrlStateNotificationService,
    AnalyticsService,
    TRACKED_EVENT_LIST,
    DynamicPopupService
} from 'app/shared/services';
import { UrlPathId } from 'app/shared/models';
import { FOCUS_TYPE } from './application-list-for-header.component';
import { ServerErrorPopupContainerComponent } from 'app/core/components/server-error-popup/server-error-popup-container.component';
import { isEmpty } from 'app/core/utils/util';
import { getApplicationList, getFavApplicationList, initApplicationList, initFavoriteApplicationList } from 'app/shared/store/actions';

@Component({
    selector: 'pp-application-list-for-header-container',
    templateUrl: './application-list-for-header-container.component.html',
    styleUrls: ['./application-list-for-header-container.component.css'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class ApplicationListForHeaderContainerComponent implements OnInit, AfterViewInit, OnDestroy {
    @ViewChild('inputQuery', {static: true}) inputQuery: ElementRef;
    private unsubscribe = new Subject<void>();
    private maxIndex: number;
    private minLength = 3;
    private filterStr = '';
    private initApplication: IApplication;
    private applicationList: IApplication[] = [];
    private favoriteApplicationList: IApplication[] = [];

    i18nText: { [key: string]: string } = {
        FAVORITE_LIST_TITLE: '',
        APPLICATION_LIST_TITLE: '',
        INPUT_APPLICATION_NAME: '',
        SELECT_APPLICATION: '',
        EMPTY_LIST: ''
    };
    showTitle = true;
    selectedApplication: IApplication;
    focusType: FOCUS_TYPE = FOCUS_TYPE.KEYBOARD;
    focusIndex = -1;
    hide = true;
    filteredApplicationList: IApplication[] = [];
    filteredFavoriteApplicationList: IApplication[] = [];
    funcImagePath: Function;
    useDisable = true;
    showLoading = true;
    selectedAppIcon: string;
    selectedAppName: string;

    constructor(
        private cd: ChangeDetectorRef,
        private storeHelperService: StoreHelperService,
        private webAppSettingDataService: WebAppSettingDataService,
        private newUrlStateNotificationService: NewUrlStateNotificationService,
        private urlRouteManagerService: UrlRouteManagerService,
        private translateService: TranslateService,
        private analyticsService: AnalyticsService,
        private renderer: Renderer2,
        private dynamicPopupService: DynamicPopupService,
        private componentFactoryResolver: ComponentFactoryResolver,
        private injector: Injector
    ) {}

    ngOnInit() {
        this.initI18nText();
        this.funcImagePath = this.webAppSettingDataService.getIconPathMakeFunc();

        this.newUrlStateNotificationService.onUrlStateChange$.pipe(
            takeUntil(this.unsubscribe)
        ).subscribe((urlService: NewUrlStateNotificationService) => {
            if (urlService.hasValue(UrlPathId.APPLICATION)) {
                this.initApplication = urlService.getPathValue(UrlPathId.APPLICATION);
                this.selectApplication(this.initApplication);
                this.toggleApplicationList({open: false});
            } else {
                this.toggleApplicationList({open: true});
                this.selectApplication(null);
            }

            this.cd.detectChanges();
        });
        this.connectStore();
    }

    ngAfterViewInit() {
        this.setFocusToInput();
        this.bindUserInputEvent();
    }

    ngOnDestroy() {
        this.unsubscribe.next();
        this.unsubscribe.complete();
    }

    private connectStore(): void {
        combineLatest(
            this.storeHelperService.getApplicationList(this.unsubscribe).pipe(
                skipWhile((list: IApplication[]) => list === null)
                // filter((appList: IApplication[]) => !isEmpty(appList))
            ),
            this.storeHelperService.getFavoriteApplicationList(this.unsubscribe).pipe(
                skipWhile((list: IApplication[]) => list === null)
            )
        ).pipe(
            map(([appList, favAppList]: IApplication[][]) => {
                const validFavAppList = favAppList.filter((favApp: IApplication) => {
                    return appList.some((app: IApplication) => app.equals(favApp));
                });

                return {appList, favAppList: validFavAppList};
            }),
            takeUntil(this.unsubscribe)
        ).subscribe(({appList, favAppList}: {appList: IApplication[], favAppList: IApplication[]}) => {
            this.hideProcessing();
            this.refreshList(appList, favAppList);
            this.cd.detectChanges();
        });

        this.storeHelperService.getApplicationListError().pipe(
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
                    this.storeHelperService.dispatch(initApplicationList());
                }
            }, {
                resolver: this.componentFactoryResolver,
                injector: this.injector
            });
        });

        this.storeHelperService.getFavoriteApplicationListError().pipe(
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

    private bindUserInputEvent(): void {
        fromEvent(this.inputQuery.nativeElement, 'keyup').pipe(
            takeUntil(this.unsubscribe),
            debounceTime(300),
            distinctUntilChanged(),
            filter((event: KeyboardEvent) => !this.isArrowKey(event.keyCode)),
            pluck('target', 'value'),
            filter((value: string) => this.isLengthValid(value.trim().length))
        ).subscribe((value: string) => {
            this.applyQuery(value);
            this.cd.detectChanges();
        });
    }

    private initI18nText(): void {
        forkJoin(
            this.translateService.get('COMMON.INPUT_APP_NAME_PLACE_HOLDER'),
            this.translateService.get('MAIN.APP_LIST'),
            this.translateService.get('MAIN.FAVORITE_APP_LIST'),
            this.translateService.get('COMMON.SELECT_YOUR_APP'),
            this.translateService.get('COMMON.EMPTY_ON_SEARCH')
        ).subscribe((i18n: string[]) => {
            this.i18nText.INPUT_APPLICATION_NAME = i18n[0];
            this.i18nText.APPLICATION_LIST_TITLE = i18n[1];
            this.i18nText.FAVORITE_LIST_TITLE = i18n[2];
            this.i18nText.SELECT_APPLICATION = i18n[3];
            this.i18nText.EMPTY_LIST = i18n[4];
        });
    }

    private refreshList(applicationList: IApplication[], favoriteList: IApplication[]): void {
        this.applicationList = applicationList;
        this.favoriteApplicationList = favoriteList;
        this.filteredApplicationList = this.filterList(this.applicationList);
        this.filteredFavoriteApplicationList = this.filterList(this.favoriteApplicationList);
        this.maxIndex = this.filteredApplicationList.length + this.filteredFavoriteApplicationList.length;
    }

    private selectApplication(application: IApplication): void {
        this.selectedApplication = application;
        if (this.selectedApplication) {
            this.selectedAppIcon = this.funcImagePath(this.selectedApplication.getServiceType());
            this.selectedAppName = this.selectedApplication.getApplicationName();
        } else {
            this.selectedAppName = this.i18nText.SELECT_APPLICATION;
        }
    }

    private filterList(appList: IApplication[]): IApplication[] {
        if (this.filterStr === '') {
            return appList;
        } else {
            return appList.filter((application: IApplication) => {
                return new RegExp(this.filterStr, 'i').test(application.getApplicationName());
            });
        }
    }

    private setFocusToInput(): void {
        of(1).pipe(delay(0)).subscribe((v: number) => {
            this.inputQuery.nativeElement.select();
        });
    }

    private applyQuery(query: string): void {
        this.filterStr = query;
        this.filteredFavoriteApplicationList = this.filterList(this.favoriteApplicationList);
        this.filteredApplicationList = this.filterList(this.applicationList);
        this.maxIndex = this.filteredApplicationList.length + this.filteredFavoriteApplicationList.length;
        this.focusIndex = -1;
    }


    toggleApplicationList({open}: {open: boolean} = {open: this.hide}): void {
        this.hide = !open;
        if (this.hide === false) {
            this.setFocusToInput();
        }

        if (open) {
            const isAppListEmpty = isEmpty(this.applicationList);

            if (isAppListEmpty) {
                this.showProcessing();
                this.storeHelperService.dispatch(getApplicationList());
                this.storeHelperService.dispatch(getFavApplicationList());
            }
        }
    }

    onClose(): void {
        this.toggleApplicationList({open: false});
    }

    onSelectApplication(selectedApplication: IApplication): void {
        this.toggleApplicationList({open: false});
        if (!selectedApplication.equals(this.selectedApplication)) {
            this.analyticsService.trackEvent(TRACKED_EVENT_LIST.SELECT_APPLICATION);
            this.urlRouteManagerService.changeApplication(selectedApplication.getUrlStr());
            this.selectApplication(selectedApplication);
        }
    }

    onFocused(index: number): void {
        this.focusIndex = index;
        this.focusType = FOCUS_TYPE.MOUSE;
    }

    onKeyDown(keyCode: number): void {
        if (!this.hide) {
            switch (keyCode) {
                case 27: // ESC
                    this.renderer.setProperty(this.inputQuery.nativeElement, 'value', '');
                    this.applyQuery('');
                    this.toggleApplicationList({open: false});
                    break;
                case 13: // Enter
                    if (this.focusIndex !== -1) {
                        const favoriteLen = this.filteredFavoriteApplicationList.length;
                        if (favoriteLen === 0 || this.focusIndex > favoriteLen) {
                            this.onSelectApplication(this.filteredApplicationList[this.focusIndex - favoriteLen]);
                        } else {
                            this.onSelectApplication(this.filteredFavoriteApplicationList[this.focusIndex]);
                        }
                    }
                    break;
                case 38: // ArrowUp
                    if (this.focusIndex - 1 >= 0) {
                        this.focusIndex -= 1;
                        this.focusType = FOCUS_TYPE.KEYBOARD;
                    }
                    break;
                case 40: // ArrowDown
                    if (this.focusIndex + 1 < this.maxIndex) {
                        this.focusIndex += 1;
                        this.focusType = FOCUS_TYPE.KEYBOARD;
                    }
                    break;
            }
        }
    }

    onReload(): void {
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.CLICK_RELOAD_APPLICATION_LIST_BUTTON);
        this.showProcessing();
        this.storeHelperService.dispatch(getApplicationList(true));
    }

    private isArrowKey(key: number): boolean {
        return key >= 37 && key <= 40;
    }

    private isLengthValid(length: number): boolean {
        return length === 0 || length >= this.minLength;
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
