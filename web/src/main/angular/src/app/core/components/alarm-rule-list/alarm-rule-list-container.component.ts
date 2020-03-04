import { Component, OnInit, OnDestroy } from '@angular/core';
import { Subject, forkJoin } from 'rxjs';
import { TranslateService } from '@ngx-translate/core';

import { TranslateReplaceService, AnalyticsService, TRACKED_EVENT_LIST } from 'app/shared/services';
import { UserGroupDataService, IUserGroup } from 'app/core/components/user-group/user-group-data.service';
import { ApplicationListInteractionForConfigurationService } from 'app/core/components/application-list/application-list-interaction-for-configuration.service';
import { NotificationType, IAlarmForm } from './alarm-rule-create-and-update.component';
import { AlarmRuleDataService, IAlarmRule, IAlarmRuleCreated, IAlarmRuleResponse } from './alarm-rule-data.service';
import { isThatType } from 'app/core/utils/util';
import { takeUntil } from 'rxjs/operators';

@Component({
    selector: 'pp-alarm-rule-list-container',
    templateUrl: './alarm-rule-list-container.component.html',
    styleUrls: ['./alarm-rule-list-container.component.css'],
})
export class AlarmRuleListContainerComponent implements OnInit, OnDestroy {
    private unsubscribe = new Subject<void>();
    private selectedApplication: IApplication = null;

    useDisable = false;
    showLoading = false;
    showPopup = false;
    errorMessage: string;
    checkerList: string[];
    userGroupList: string[];
    alarmRuleList: IAlarmRule[] = [];
    i18nLabel = {
        CHECKER_LABEL: '',
        USER_GROUP_LABEL: '',
        THRESHOLD_LABEL: '',
        TYPE_LABEL: '',
        NOTES_LABEL: '',
    };
    i18nTemplateGuide = {
        APP_NOT_SELECTED: '',
        NO_ALARM_RESGISTERED: ''
    };
    i18nFormGuide: {[key: string]: IFormFieldErrorType};
    editAlarm: IAlarmRule;

    constructor(
        private translateService: TranslateService,
        private translateReplaceService: TranslateReplaceService,
        private alarmRuleDataService: AlarmRuleDataService,
        private userGroupDataSerivce: UserGroupDataService,
        private applicationListInteractionForConfigurationService: ApplicationListInteractionForConfigurationService,
        private analyticsService: AnalyticsService,
    ) {}

    ngOnInit() {
        this.loadCheckerList();
        this.loadUserGroupList();
        this.bindToAppSelectionEvent();
        this.initI18NText();
    }

    ngOnDestroy() {
        this.unsubscribe.next();
        this.unsubscribe.complete();
    }

    private loadCheckerList(): void {
        this.alarmRuleDataService.getCheckerList().subscribe((result: string[] | IServerErrorShortFormat) => {
            isThatType<IServerErrorShortFormat>(result, 'errorCode', 'errorMessage')
                ? this.errorMessage = result.errorMessage
                : this.checkerList = result as string[];
        }, (error: IServerErrorFormat) => {
            this.errorMessage = error.exception.message;
        });
    }

    private loadUserGroupList(): void {
        this.userGroupDataSerivce.retrieve().subscribe((result: IUserGroup[] | IServerErrorShortFormat) => {
            isThatType<IServerErrorShortFormat>(result, 'errorCode', 'errorMessage')
                ? this.errorMessage = result.errorMessage
                : this.userGroupList = result.map((userGroup: IUserGroup) => userGroup.id);
        }, (error: IServerErrorFormat) => {
            this.errorMessage = error.exception.message;
        });
    }

    private bindToAppSelectionEvent(): void {
        this.applicationListInteractionForConfigurationService.onSelectApplication$.pipe(
            takeUntil(this.unsubscribe)
        ).subscribe((selectedApplication: IApplication) => {
            this.selectedApplication = selectedApplication;
            this.errorMessage = '';
            this.onClosePopup();
            this.getAlarmData();
        });
    }

    private initI18NText(): void {
        forkJoin(
            this.translateService.get('COMMON.REQUIRED_SELECT'),
            this.translateService.get('CONFIGURATION.COMMON.CHECKER'),
            this.translateService.get('CONFIGURATION.COMMON.USER_GROUP'),
            this.translateService.get('CONFIGURATION.COMMON.THRESHOLD'),
            this.translateService.get('CONFIGURATION.COMMON.TYPE'),
            this.translateService.get('CONFIGURATION.COMMON.NOTES'),
            this.translateService.get('CONFIGURATION.ALARM.EMPTY'),
            this.translateService.get('COMMON.SELECT_YOUR_APP')
        ).subscribe(([requiredMessage, checkerLabel, userGroupLabel, thresholdLabel, typeLabel, notesLabel, alarmEmpty, selectApp]: string[]) => {
            this.i18nFormGuide = {
                checkerName: { required: this.translateReplaceService.replace(requiredMessage, checkerLabel) },
                userGroupId: { required: this.translateReplaceService.replace(requiredMessage, userGroupLabel) },
                threshold: {
                    required: this.translateReplaceService.replace(requiredMessage, thresholdLabel),
                    min: 'Must be greater than 0'
                },
                type: { required: this.translateReplaceService.replace(requiredMessage, typeLabel) }
            };

            this.i18nLabel.CHECKER_LABEL = checkerLabel;
            this.i18nLabel.USER_GROUP_LABEL = userGroupLabel;
            this.i18nLabel.THRESHOLD_LABEL = thresholdLabel;
            this.i18nLabel.TYPE_LABEL = typeLabel;
            this.i18nLabel.NOTES_LABEL = notesLabel;

            this.i18nTemplateGuide.NO_ALARM_RESGISTERED = alarmEmpty;
            this.i18nTemplateGuide.APP_NOT_SELECTED = selectApp;
        });
    }

    private getAlarmData(): void {
        this.showProcessing();
        this.alarmRuleDataService.retrieve(this.selectedApplication.getApplicationName()).subscribe((result: IAlarmRule[] | IServerErrorShortFormat) => {
            isThatType<IServerErrorShortFormat>(result, 'errorCode', 'errorMessage')
                ? this.errorMessage = result.errorMessage
                : this.alarmRuleList = result;
            this.hideProcessing();
        }, (error: IServerErrorFormat) => {
            this.hideProcessing();
            this.errorMessage = error.exception.message;
        });
    }

    onCreateAlarm({checkerName, userGroupId, threshold, type, notes}: IAlarmForm): void {
        this.showProcessing();
        this.alarmRuleDataService.create({
            applicationId: this.selectedApplication.getApplicationName(),
            serviceType: this.selectedApplication.getServiceType(),
            checkerName,
            userGroupId,
            threshold,
            emailSend: type === NotificationType.ALL || type === NotificationType.EMAIL,
            smsSend: type === NotificationType.ALL || type === NotificationType.SMS,
            notes
        }).subscribe((response: IAlarmRuleCreated | IServerErrorShortFormat) => {
            if (isThatType<IServerErrorShortFormat>(response, 'errorCode', 'errorMessage')) {
                this.errorMessage = response.errorMessage;
                this.hideProcessing();
            } else {
                this.getAlarmData();
                this.analyticsService.trackEvent(TRACKED_EVENT_LIST.CREATE_ALARM);
            }
        }, (error: IServerErrorFormat) => {
            this.hideProcessing();
            this.errorMessage = error.exception.message;
        });
    }

    onUpdateAlarm({checkerName, userGroupId, threshold, type, notes}: IAlarmForm): void {
        this.showProcessing();
        const {ruleId, applicationId, serviceType} = this.editAlarm;

        this.alarmRuleDataService.update({
            ruleId,
            applicationId,
            serviceType,
            checkerName,
            userGroupId,
            threshold,
            emailSend: type === NotificationType.ALL || type === NotificationType.EMAIL,
            smsSend: type === NotificationType.ALL || type === NotificationType.SMS,
            notes
        }).subscribe((response: IAlarmRuleResponse | IServerErrorShortFormat) => {
            if (isThatType<IServerErrorShortFormat>(response, 'errorCode', 'errorMessage')) {
                this.errorMessage = (response as IServerErrorShortFormat).errorMessage;
                this.hideProcessing();
            } else {
                this.getAlarmData();
                this.analyticsService.trackEvent(TRACKED_EVENT_LIST.UPDATE_ALARM);
            }
        }, (error: IServerErrorFormat) => {
            this.hideProcessing();
            this.errorMessage = error.exception.message;
        });
    }

    onClickAddBtn(): void {
        this.editAlarm = null;
        this.showPopup = true;
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.SHOW_ALARM_CREATION_POPUP);
    }

    onClosePopup(): void {
        this.showPopup = false;
    }

    onCloseErrorMessage(): void {
        this.errorMessage = '';
    }

    onRemoveAlarm(ruleId: string): void {
        this.showProcessing();
        this.alarmRuleDataService.remove(this.selectedApplication.getApplicationName(), ruleId).subscribe((response: IAlarmRuleResponse | IServerErrorShortFormat) => {
            if (isThatType<IServerErrorShortFormat>(response, 'errorCode', 'errorMessage')) {
                this.errorMessage = (response as IServerErrorShortFormat).errorMessage;
                this.hideProcessing();
            } else {
                this.getAlarmData();
                this.analyticsService.trackEvent(TRACKED_EVENT_LIST.REMOVE_ALARM);
            }
        }, (error: IServerErrorFormat) => {
            this.hideProcessing();
            this.errorMessage = error.exception.message;
        });
    }

    onEditAlarm(ruleId: string): void {
        this.editAlarm = this.alarmRuleList.find(({ruleId: alarmId}: IAlarmRule) => alarmId === ruleId);
        this.showPopup = true;
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.SHOW_ALARM_UPDATE_POPUP);
    }

    isApplicationSelected(): boolean {
        return this.selectedApplication !== null;
    }

    showGuide(): boolean {
        return !this.isApplicationSelected() || this.alarmRuleList.length === 0;
    }

    get guideMessage(): string {
        return !this.isApplicationSelected() ? this.i18nTemplateGuide.APP_NOT_SELECTED : this.i18nTemplateGuide.NO_ALARM_RESGISTERED;
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
