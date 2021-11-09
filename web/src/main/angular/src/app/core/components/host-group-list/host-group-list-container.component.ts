import { Component, OnInit, AfterViewInit, ViewChild, ElementRef, OnDestroy, Renderer2, ComponentFactoryResolver, Injector } from '@angular/core';
import { Subject, fromEvent, of, forkJoin } from 'rxjs';
import { debounceTime, distinctUntilChanged, filter, takeUntil, pluck, delay } from 'rxjs/operators';
import { TranslateService } from '@ngx-translate/core';

import {
    StoreHelperService,
    UrlRouteManagerService,
    NewUrlStateNotificationService,
    AnalyticsService,
    TRACKED_EVENT_LIST,
    DynamicPopupService,
    WebAppSettingDataService
} from 'app/shared/services';
import { UrlPathId } from 'app/shared/models';
import { FOCUS_TYPE } from './host-group-list.component';
import { ServerErrorPopupContainerComponent } from 'app/core/components/server-error-popup/server-error-popup-container.component';
import { isEmpty } from 'app/core/utils/util';
import { getHostGroupList, initHostGroupList } from 'app/shared/store/actions';

@Component({
    selector: 'pp-host-group-list-container',
    templateUrl: './host-group-list-container.component.html',
    styleUrls: ['./host-group-list-container.component.css'],
})
export class HostGroupListContainerComponent implements OnInit, AfterViewInit, OnDestroy {
    @ViewChild('inputQuery', {static: true}) inputQuery: ElementRef;
    private unsubscribe = new Subject<void>();
    private maxIndex: number;
    private minLength = 3;
    private filterStr = '';
    private hostGroupList: string[] = [];

    filteredHostGroupList: string[] = [];
    i18nText: {[key: string]: string} = {
        HOST_GROUP_LIST_TITLE: '',
        INPUT_HOST_GROUP_NAME: '',
        SELECT_HOST_GROUP: '',
        EMPTY_LIST: ''
    };
    showTitle = true;
    selectedHostGroup: string;
    focusType: FOCUS_TYPE = FOCUS_TYPE.KEYBOARD;
    focusIndex = -1;
    hide = true;
    useDisable = true;
    showLoading = true;

    constructor(
        private storeHelperService: StoreHelperService,
        private newUrlStateNotificationService: NewUrlStateNotificationService,
        private urlRouteManagerService: UrlRouteManagerService,
        private translateService: TranslateService,
        private analyticsService: AnalyticsService,
        private webAppSettingDataService: WebAppSettingDataService,
        private renderer: Renderer2,
        private dynamicPopupService: DynamicPopupService,
        private componentFactoryResolver: ComponentFactoryResolver,
        private injector: Injector
    ) {}

    ngOnInit() {
        this.initI18nText();

        this.newUrlStateNotificationService.onUrlStateChange$.pipe(
            takeUntil(this.unsubscribe)
        ).subscribe((urlService: NewUrlStateNotificationService) => {
            if (urlService.hasValue(UrlPathId.HOST_GROUP)) {
                this.selectedHostGroup = urlService.getPathValue(UrlPathId.HOST_GROUP);
                this.toggleHostGroupList({open: false});
            } else {
                this.toggleHostGroupList({open: true});
                this.selectedHostGroup = '';
            }

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
        this.storeHelperService.getHostGroupList().pipe(
            filter((hostGroupList: string[]) => !isEmpty(hostGroupList)),
            takeUntil(this.unsubscribe)
        ).subscribe((hostGroupList: string[]) => {
            this.hideProcessing();
            this.hostGroupList = hostGroupList;
            this.filteredHostGroupList = this.filterList(hostGroupList);
            this.maxIndex = this.filteredHostGroupList.length;
        });

        this.storeHelperService.getHostGroupListError().pipe(
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
                    this.storeHelperService.dispatch(initHostGroupList());
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
        });
    }

    private initI18nText(): void {
        forkJoin(
            this.translateService.get('METRIC.INPUT_HOST_GROUP_NAME_PLACE_HOLDER'),
            this.translateService.get('METRIC.HOST_GROUP_LIST'),
            this.translateService.get('METRIC.SELECT_HOST_GROUP'),
            this.translateService.get('COMMON.EMPTY_ON_SEARCH')
        ).subscribe((i18n: string[]) => {
            this.i18nText.INPUT_HOST_GROUP_NAME = i18n[0];
            this.i18nText.HOST_GROUP_LIST_TITLE = i18n[1];
            this.i18nText.SELECT_HOST_GROUP = i18n[2];
            this.i18nText.EMPTY_LIST = i18n[3];
        });
    }

    private filterList(hostGroupList: string[]): string[] {
        if (this.filterStr === '') {
            return hostGroupList;
        } else {
            return hostGroupList.filter((hostGroup: string) => new RegExp(this.filterStr, 'i').test(hostGroup));
        }
    }

    private setFocusToInput(): void {
        of(1).pipe(delay(0)).subscribe(() => {
            this.inputQuery.nativeElement.select();
        });
    }

    private applyQuery(query: string): void {
        this.filterStr = query;
        this.filteredHostGroupList = this.filterList(this.hostGroupList);
        this.maxIndex = this.filteredHostGroupList.length;
        this.focusIndex = -1;
    }

    toggleHostGroupList({open}: {open: boolean} = {open: this.hide}): void {
        this.hide = !open;
        if (this.hide === false) {
            this.setFocusToInput();
        }

        if (open) {
            const isHostGroupListEmpty = isEmpty(this.hostGroupList);

            if (isHostGroupListEmpty) {
                this.showProcessing();
                this.storeHelperService.dispatch(getHostGroupList());
            }
        }
    }

    onClose(): void {
        this.toggleHostGroupList({open: false});
    }

    onSelectHostGroup(selectedHostGroup: string): void {
        this.toggleHostGroupList({open: false});
        if (this.selectedHostGroup === selectedHostGroup) {
            return;
        }

        this.selectedHostGroup = selectedHostGroup;
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.SELECT_HOST_GROUP);
        this.urlRouteManagerService.move({
            url: [
                this.newUrlStateNotificationService.getStartPath(),
                selectedHostGroup,
                this.webAppSettingDataService.getUserDefaultPeriod().getValueWithTime(),
            ],
            needServerTimeRequest: true
        });
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
                    this.toggleHostGroupList({open: false});
                    break;
                case 13: // Enter
                    if (this.focusIndex !== -1) {
                        this.onSelectHostGroup(this.filteredHostGroupList[this.focusIndex]);
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
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.CLICK_RELOAD_HOST_GROUP_LIST_BUTTON);
        this.showProcessing();
        this.storeHelperService.dispatch(getHostGroupList(true));
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
