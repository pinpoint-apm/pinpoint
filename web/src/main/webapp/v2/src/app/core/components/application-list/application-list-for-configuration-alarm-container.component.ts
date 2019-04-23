import { Component, OnInit, AfterViewInit, ViewChild, ElementRef, ChangeDetectorRef, ChangeDetectionStrategy, OnDestroy, Renderer2 } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { Subject, combineLatest, fromEvent } from 'rxjs';
import { debounceTime, distinctUntilChanged, filter, pluck } from 'rxjs/operators';

import { WebAppSettingDataService, StoreHelperService, AnalyticsService, TRACKED_EVENT_LIST } from 'app/shared/services';
import { ApplicationListInteractionForConfigurationService } from './application-list-interaction-for-configuration.service';
import { FOCUS_TYPE } from './application-list-for-header.component';

@Component({
    selector: 'pp-application-list-for-configuration-alarm-container',
    templateUrl: './application-list-for-configuration-alarm-container.component.html',
    styleUrls: ['./application-list-for-configuration-alarm-container.component.css'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class ApplicationListForConfigurationAlarmContainerComponent implements OnInit, AfterViewInit, OnDestroy {
    @ViewChild('inputQuery') inputQuery: ElementRef;
    i18nText: { [key: string]: string } = {
        INPUT_APPLICATION_NAME: '',
        SELECTED_APPLICATION_NAME: '',
        EMPTY_LIST: ''
    };
    private unsubscribe: Subject<null> = new Subject();
    private minLength = 3;
    private filterStr = '';
    private applicationList: IApplication[];
    filteredApplicationList: IApplication[];
    selectedApplication: IApplication;
    showTitle = false;
    focusType: FOCUS_TYPE = FOCUS_TYPE.KEYBOARD;
    restCount = 0;
    focusIndex = -1;
    funcImagePath: Function;

    constructor(
        private changeDetector: ChangeDetectorRef,
        private renderer: Renderer2,
        private storeHelperService: StoreHelperService,
        private webAppSettingDataService: WebAppSettingDataService,
        private translateService: TranslateService,
        private applicationListInteractionForConfigurationService: ApplicationListInteractionForConfigurationService,
        private analyticsService: AnalyticsService,
    ) {}
    ngOnInit() {
        this.initI18nText();
        this.funcImagePath = this.webAppSettingDataService.getIconPathMakeFunc();
        // this.initIconData();

        this.storeHelperService.getApplicationList(this.unsubscribe).subscribe((applicationList: IApplication[]) => {
            this.applicationList = applicationList;
            this.filteredApplicationList = this.filterList(this.applicationList);
            this.changeDetector.detectChanges();
        });
    }
    ngAfterViewInit() {
        this.bindUserInputEvent();
    }
    ngOnDestroy() {
        this.unsubscribe.next();
        this.unsubscribe.complete();
    }
    private bindUserInputEvent(): void {
        fromEvent(this.inputQuery.nativeElement, 'keyup').pipe(
            debounceTime(300),
            filter((event: KeyboardEvent) => {
                return !this.isArrowKey(event.keyCode);
            }),
            pluck('target', 'value'),
            filter((value: string) => {
                return this.isLengthValid(value.trim().length);
            }),
            distinctUntilChanged()
        ).subscribe((value: string) => {
            this.applyQuery(value);
        });
    }
    private initI18nText(): void {
        combineLatest(
            this.translateService.get('MAIN.INPUT_APP_NAME_PLACE_HOLDER'),
            this.translateService.get('MAIN.APP_LIST'),
            this.translateService.get('CONFIGURATION.GENERAL.EMPTY')
        ).subscribe((i18n: string[]) => {
            this.i18nText.INPUT_APPLICATION_NAME = i18n[0];
            this.i18nText.APPLICATION_LIST_TITLE = i18n[1];
            this.i18nText.EMPTY_LIST = i18n[2];
        });
    }
    private selectApplication(application: IApplication): void {
        if (application) {
            this.selectedApplication = application;
            this.changeDetector.detectChanges();
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
    private applyQuery(query: string): void {
        this.filterStr = query;
        this.filteredApplicationList = this.filterList(this.applicationList);
        this.focusIndex = -1;
        this.changeDetector.detectChanges();
    }

    getSelectedApplicationIcon(): string {
        return this.funcImagePath(this.selectedApplication.getServiceType());
    }

    getSelectedApplicationName(): string {
        if (this.selectedApplication) {
            return this.selectedApplication.getApplicationName();
        } else {
            return this.i18nText.SELECTED_APPLICATION_NAME;
        }
    }
    onSelectApplication(selectedApplication: IApplication): void {
        this.selectApplication(selectedApplication);
        this.applicationListInteractionForConfigurationService.setSelectedApplication(selectedApplication);
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.SELECT_APPLICATION_FOR_ALARM);
    }
    onFocused(index: number): void {
        this.focusIndex = index;
        this.focusType = FOCUS_TYPE.MOUSE;
        this.changeDetector.detectChanges();
    }
    onKeyDown(keyCode: number): void {
        switch (keyCode) {
            case 27: // ESC
                this.renderer.setProperty(this.inputQuery.nativeElement, 'value', '');
                this.applyQuery('');
                this.changeDetector.detectChanges();
                break;
            // case 13: // Enter
            //     if (this.focusIndex !== -1) {
            //         this.onSelectApplication(this.filteredApplicationList[this.focusIndex]);
            //         this.changeDetector.detectChanges();
            //     }
            //     break;
            // case 38: // ArrowUp
            //     if (this.focusIndex - 1 >= 0) {
            //         this.focusIndex -= 1;
            //         this.focusType = FOCUS_TYPE.KEYBOARD;
            //         this.changeDetector.detectChanges();
            //     }
            //     break;
            // case 40: // ArrowDown
            //     if (this.focusIndex + 1 < this.filteredApplicationList.length) {
            //         this.focusIndex += 1;
            //         this.focusType = FOCUS_TYPE.KEYBOARD;
            //         this.changeDetector.detectChanges();
            //     }
            //     break;
        }
    }
    private isArrowKey(key: number): boolean {
        return key >= 37 && key <= 40;
    }
    private isLengthValid(length: number): boolean {
        return length === 0 || length >= this.minLength;
    }
}
