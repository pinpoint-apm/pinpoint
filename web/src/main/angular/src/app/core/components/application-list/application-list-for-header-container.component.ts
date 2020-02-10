import { Component, OnInit, AfterViewInit, ViewChild, ElementRef, ChangeDetectorRef, ChangeDetectionStrategy, OnDestroy, Renderer2 } from '@angular/core';
import { Subject, combineLatest, fromEvent, of, forkJoin } from 'rxjs';
import { debounceTime, distinctUntilChanged, filter, takeUntil, pluck, delay } from 'rxjs/operators';
import { TranslateService } from '@ngx-translate/core';

import {
    StoreHelperService,
    WebAppSettingDataService,
    UrlRouteManagerService,
    NewUrlStateNotificationService,
    AnalyticsService,
    TRACKED_EVENT_LIST,
    ApplicationListDataService
} from 'app/shared/services';
import { UrlPathId } from 'app/shared/models';
import { FOCUS_TYPE } from './application-list-for-header.component';

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
    private applicationList: IApplication[];
    private favoriteApplicationList: IApplication[];

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
    hiddenComponent = true;
    filteredApplicationList: IApplication[] = [];
    filteredFavoriteApplicationList: IApplication[] = [];
    funcImagePath: Function;
    showLoading = false;
    selectedAppIcon: string;
    selectedAppName: string;

    constructor(
        private cd: ChangeDetectorRef,
        private storeHelperService: StoreHelperService,
        private webAppSettingDataService: WebAppSettingDataService,
        private applicationListDataService: ApplicationListDataService,
        private newUrlStateNotificationService: NewUrlStateNotificationService,
        private urlRouteManagerService: UrlRouteManagerService,
        private translateService: TranslateService,
        private analyticsService: AnalyticsService,
        private renderer: Renderer2
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
                this.hiddenComponent = true;
            } else {
                this.hiddenComponent = false;
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
            this.storeHelperService.getApplicationList(this.unsubscribe),
            this.storeHelperService.getFavoriteApplicationList(this.unsubscribe)
        ).subscribe((responseData: any[]) => {
            this.refreshList(responseData[0], responseData[1]);
            this.showLoading = false;
            this.cd.detectChanges();
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


    toggleApplicationList(): void {
        this.hiddenComponent = !this.hiddenComponent;
        if (this.hiddenComponent === false) {
            this.setFocusToInput();
        }
    }

    onClose(): void {
        this.hiddenComponent = true;
    }

    onSelectApplication(selectedApplication: IApplication): void {
        this.hiddenComponent = true;
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
        if (!this.hiddenComponent) {
            switch (keyCode) {
                case 27: // ESC
                    this.renderer.setProperty(this.inputQuery.nativeElement, 'value', '');
                    this.applyQuery('');
                    this.hiddenComponent = true;
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
        this.showLoading = true;
        this.applicationListDataService.getApplicationList().subscribe(() => {});
    }

    private isArrowKey(key: number): boolean {
        return key >= 37 && key <= 40;
    }

    private isLengthValid(length: number): boolean {
        return length === 0 || length >= this.minLength;
    }
}
