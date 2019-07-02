import { Component, OnInit, OnDestroy, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { Subject, forkJoin } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { TranslateService } from '@ngx-translate/core';

import { TranslateReplaceService, AnalyticsService, TRACKED_EVENT_LIST } from 'app/shared/services';
import { UserGroupDataService, IUserGroup } from 'app/core/components/user-group/user-group-data.service';
import { ApplicationListInteractionForConfigurationService } from 'app/core/components/application-list/application-list-interaction-for-configuration.service';
import { Alarm } from './alarm-rule-create-and-update.component';
import { AlarmRuleDataService, IAlarmRule, IAlarmRuleCreated, IAlarmRuleResponse } from './alarm-rule-data.service';
import { isThatType } from 'app/core/utils/util';

@Component({
    selector: 'pp-alarm-rule-list-container',
    templateUrl: './alarm-rule-list-container.component.html',
    styleUrls: ['./alarm-rule-list-container.component.css'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class AlarmRuleListContainerComponent implements OnInit, OnDestroy {
    private unsubscribe: Subject<null> = new Subject();
    private editAlarmIndex: number;
    currentApplication: IApplication = null;
    useDisable = false;
    showLoading = false;
    showCreate = false;
    errorMessage: string;
    checkerList: string[];
    userGroupList: string[];
    alarmRuleList: IAlarmRule[];

    i18nLabel = {
        CHECKER_LABEL: '',
        USER_GROUP_LABEL: '',
        THRESHOLD_LABEL: '',
        TYPE_LABEL: '',
        NOTES_LABEL: '',
    };
    i18nGuide: { [key: string]: IFormFieldErrorType };
    editAlarm: any;

    constructor(
        private changeDetectorRef: ChangeDetectorRef,
        private translateService: TranslateService,
        private translateReplaceService: TranslateReplaceService,
        private alarmRuleDataService: AlarmRuleDataService,
        private userGroupDataSerivce: UserGroupDataService,
        private applicationListInteractionForConfigurationService: ApplicationListInteractionForConfigurationService,
        private analyticsService: AnalyticsService,
    ) {}
    ngOnInit() {
        this.alarmRuleDataService.getCheckerList().pipe(
            takeUntil(this.unsubscribe)
        ).subscribe((result: string[] | IServerErrorShortFormat) => {
            isThatType<IServerErrorShortFormat>(result, 'errorCode', 'errorMessage')
                ? this.errorMessage = result.errorMessage
                : this.checkerList = result;
        });

        this.userGroupDataSerivce.retrieve().pipe(
            takeUntil(this.unsubscribe)
        ).subscribe((result: IUserGroup[] | IServerErrorShortFormat) => {
            isThatType<IServerErrorShortFormat>(result, 'errorCode', 'errorMessage')
                ? this.errorMessage = result.errorMessage
                : this.userGroupList = result.map((userGroup: IUserGroup) => userGroup.id);
        }, (_: IServerErrorFormat) => {});

        this.applicationListInteractionForConfigurationService.onSelectApplication$.pipe(
            takeUntil(this.unsubscribe)
        ).subscribe((selectedApplication: IApplication) => {
            this.currentApplication = selectedApplication;
            this.onCloseCreateAlarmPopup();
            this.getAlarmData();
        });
        this.getI18NText();
    }
    ngOnDestroy() {
        this.unsubscribe.next();
        this.unsubscribe.complete();
    }
    private getI18NText(): void {
        forkJoin(
            this.translateService.get('COMMON.REQUIRED_SELECT'),
            this.translateService.get('CONFIGURATION.COMMON.CHECKER'),
            this.translateService.get('CONFIGURATION.COMMON.USER_GROUP'),
            this.translateService.get('CONFIGURATION.COMMON.THRESHOLD'),
            this.translateService.get('CONFIGURATION.COMMON.TYPE'),
            this.translateService.get('CONFIGURATION.COMMON.NOTES'),
        ).subscribe(([requiredMessage, checkerLabel, userGroupLabel, thresholdLabel, typeLabel, notesLabel]: string[]) => {
            this.i18nGuide = {
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
        });
    }
    private getAlarmData(): void {
        this.showProcessing();
        this.alarmRuleDataService.retrieve(this.currentApplication.getApplicationName())
            .subscribe((result: IAlarmRule[] | IServerErrorShortFormat) => {
                isThatType<IServerErrorShortFormat>(result, 'errorCode', 'errorMessage')
                    ? this.errorMessage = result.errorMessage
                    : this.alarmRuleList = result;
                this.hideProcessing();
                this.changeDetectorRef.detectChanges();
            }, (error: IServerErrorFormat) => {
                this.hideProcessing();
                this.errorMessage = error.exception.message;
                this.changeDetectorRef.detectChanges();
            });
    }
    private getAlarmIndexByRuleId(ruleId: string): number {
        let index = -1;
        for (let i = 0 ; i < this.alarmRuleList.length ; i++) {
            if (this.alarmRuleList[i].ruleId === ruleId) {
                index = i;
                break;
            }
        }
        return index;
    }
    private getTypeStr(smsSend: boolean, emailSend: boolean): string {
        if (smsSend && emailSend) {
            return 'all';
        } else {
            if (smsSend) {
                return 'sms';
            }
            if (emailSend) {
                return 'email';
            }
            return 'none';
        }
    }
    onCreateAlarm(alarm: Alarm): void {
        this.showProcessing();
        this.alarmRuleDataService.create({
            applicationId: this.currentApplication.getApplicationName(),
            serviceType: this.currentApplication.getServiceType(),
            userGroupId: alarm.userGroupId,
            checkerName: alarm.checkerName,
            threshold: alarm.threshold,
            smsSend: alarm.smsSend,
            emailSend: alarm.emailSend,
            notes: alarm.notes
        } as IAlarmRule).subscribe((response: IAlarmRuleCreated | IServerErrorShortFormat) => {
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
    onUpdateAlarm(alarm: Alarm): void {
        const editAlarm = this.alarmRuleList[this.editAlarmIndex];
        this.alarmRuleDataService.update({
            applicationId: editAlarm.applicationId,
            ruleId: editAlarm.ruleId,
            serviceType: editAlarm.serviceType,
            checkerName: alarm.checkerName,
            userGroupId: alarm.userGroupId,
            threshold: alarm.threshold,
            smsSend: alarm.smsSend,
            emailSend: alarm.emailSend,
            notes: alarm.notes
        } as IAlarmRule).subscribe((response: IAlarmRuleResponse | IServerErrorShortFormat) => {
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
    onShowCreateAlarmPopup(): void {
        if (this.isApplicationSelected() === false) {
            return;
        }
        this.showCreate = true;
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.SHOW_ALARM_CREATION_POPUP);
    }
    onCloseCreateAlarmPopup(): void {
        this.showCreate = false;
    }
    onCloseErrorMessage(): void {
        this.errorMessage = '';
    }
    onReload(): void {
        this.getAlarmData();
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.RELOAD_ALARM_LIST);
    }
    onRemoveAlarm(ruleId: string): void {
        this.showProcessing();
        this.alarmRuleDataService.remove(this.currentApplication.getApplicationName(), ruleId).subscribe((response: IAlarmRuleResponse | IServerErrorShortFormat) => {
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
        this.editAlarmIndex = this.getAlarmIndexByRuleId(ruleId);
        const editAlarm = this.alarmRuleList[this.editAlarmIndex];
        this.editAlarm = new Alarm(
            editAlarm.checkerName,
            editAlarm.userGroupId,
            editAlarm.threshold,
            this.getTypeStr(editAlarm.smsSend, editAlarm.emailSend),
            editAlarm.notes
        );
        this.onShowCreateAlarmPopup();
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.SHOW_ALARM_UPDATE_POPUP);
    }
    isApplicationSelected(): boolean {
        return this.currentApplication !== null;
    }
    getAddButtonClass(): object {
        return {
            'btn-blue': this.isApplicationSelected(),
            'btn-gray': !this.isApplicationSelected()
        };
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
