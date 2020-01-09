import { Component, OnInit, AfterViewInit, ViewChild, ElementRef, OnDestroy, Renderer2 } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { Subject, fromEvent, forkJoin } from 'rxjs';
import { debounceTime, distinctUntilChanged, filter, pluck, switchMapTo } from 'rxjs/operators';

import {
    WebAppSettingDataService,
    StoreHelperService,
    AnalyticsService,
    TRACKED_EVENT_LIST,
    MessageQueueService,
    MESSAGE_TO,
    ApplicationListDataService
} from 'app/shared/services';
import { ApplicationListInteractionForConfigurationService } from './application-list-interaction-for-configuration.service';
import { FOCUS_TYPE } from './application-list-for-header.component';

@Component({
    selector: 'pp-application-list-for-agent-management-container',
    templateUrl: './application-list-for-agent-management-container.component.html',
    styleUrls: ['./application-list-for-agent-management-container.component.css']
})
export class ApplicationListForAgentManagementContainerComponent implements OnInit, AfterViewInit, OnDestroy {
    @ViewChild('inputQuery', { static: true }) inputQuery: ElementRef;

    private unsubscribe = new Subject<void>();
    private minLength = 3;
    private filterStr = '';
    private applicationList: IApplication[];

    i18nText: { [key: string]: string } = {
        INPUT_APPLICATION_NAME: '',
        SELECTED_APPLICATION_NAME: '',
        EMPTY_LIST: ''
    };
    filteredApplicationList: IApplication[];
    selectedApplication: IApplication;
    showTitle = false;
    focusType: FOCUS_TYPE = FOCUS_TYPE.KEYBOARD;
    restCount = 0;
    focusIndex = -1;
    funcImagePath: Function;

    constructor(
        private renderer: Renderer2,
        private storeHelperService: StoreHelperService,
        private webAppSettingDataService: WebAppSettingDataService,
        private translateService: TranslateService,
        private messageQueueService: MessageQueueService,
        private applicationListDataService: ApplicationListDataService,
        private applicationListInteractionForConfigurationService: ApplicationListInteractionForConfigurationService,
        private analyticsService: AnalyticsService,
    ) {}

    ngOnInit() {
        this.initI18nText();
        this.funcImagePath = this.webAppSettingDataService.getIconPathMakeFunc();
        this.storeHelperService.getApplicationList(this.unsubscribe).subscribe((applicationList: IApplication[]) => {
            this.applicationList = applicationList;
            this.filteredApplicationList = this.filterList(this.applicationList);
        });

        this.messageQueueService.receiveMessage(this.unsubscribe, MESSAGE_TO.APPLICATION_REMOVED).pipe(
            switchMapTo(this.applicationListDataService.getApplicationList())
        ).subscribe(() => {
            this.selectedApplication = null;
        });
    }

    ngAfterViewInit() {
        this.bindUserInputEvent();
    }

    ngOnDestroy() {
        this.unsubscribe.next();
        this.unsubscribe.complete();
        this.applicationListInteractionForConfigurationService.setSelectedApplication(null);
    }

    private bindUserInputEvent(): void {
        fromEvent(this.inputQuery.nativeElement, 'keyup').pipe(
            debounceTime(300),
            filter(({keyCode}: KeyboardEvent) => !this.isArrowKey(keyCode)),
            pluck('target', 'value'),
            filter((value: string) => this.isLengthValid(value.trim().length)),
            distinctUntilChanged()
        ).subscribe((value: string) => {
            this.applyQuery(value);
        });
    }

    private initI18nText(): void {
        forkJoin(
            this.translateService.get('COMMON.INPUT_APP_NAME_PLACE_HOLDER'),
            this.translateService.get('MAIN.APP_LIST'),
            this.translateService.get('COMMON.EMPTY')
        ).subscribe((i18n: string[]) => {
            this.i18nText.INPUT_APPLICATION_NAME = i18n[0];
            this.i18nText.APPLICATION_LIST_TITLE = i18n[1];
            this.i18nText.EMPTY_LIST = i18n[2];
        });
    }

    private selectApplication(application: IApplication): void {
        if (!application) {
            return;
        }

        this.selectedApplication = application;
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
    }

    getSelectedApplicationIcon(): string {
        return this.funcImagePath(this.selectedApplication.getServiceType());
    }

    onSelectApplication(selectedApplication: IApplication): void {
        if (selectedApplication.equals(this.selectedApplication)) {
            return;
        }

        this.selectApplication(selectedApplication);
        this.applicationListInteractionForConfigurationService.setSelectedApplication(selectedApplication);
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.SELECT_APPLICATION_FOR_ALARM);
    }

    onFocused(index: number): void {
        this.focusIndex = index;
        this.focusType = FOCUS_TYPE.MOUSE;
    }

    onKeyDown(keyCode: number): void {
        switch (keyCode) {
            case 27: // ESC
                this.renderer.setProperty(this.inputQuery.nativeElement, 'value', '');
                this.applyQuery('');
                break;
        }
    }

    private isArrowKey(key: number): boolean {
        return key >= 37 && key <= 40;
    }

    private isLengthValid(length: number): boolean {
        return length === 0 || length >= this.minLength;
    }
}
