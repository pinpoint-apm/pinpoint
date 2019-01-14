import { Component, OnInit, OnDestroy, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { Subject, combineLatest } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { TranslateService } from '@ngx-translate/core';

import { TranslateReplaceService } from 'app/shared/services';
import { UserGroupDataService, IUserGroup } from 'app/core/components/user-group/user-group-data.service';
import { ApplicationListInteractionForConfigurationService } from 'app/core/components/application-list/application-list-interaction-for-configuration.service';
import { Alarm } from './alarm-rule-create-and-update.component';
import { AlarmRuleDataService, IAlarmRule, IAlarmRuleCreated, IAlarmRuleResponse } from './alarm-rule-data.service';

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
    message = '';
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
    i18nGuide = {
        CHECKER_REQUIRED: '',
        USER_GROUP_REQUIRED: '',
        THRESHOLD_REQUIRED: '',
        TYPE_REQUIRED: ''
    };
    editAlarm: any;

    constructor(
        private changeDetectorRef: ChangeDetectorRef,
        private translateService: TranslateService,
        private translateReplaceService: TranslateReplaceService,
        private alarmRuleDataService: AlarmRuleDataService,
        private userGroupDataSerivce: UserGroupDataService,
        private applicationListInteractionForConfigurationService: ApplicationListInteractionForConfigurationService
    ) {}
    ngOnInit() {
        this.alarmRuleDataService.getCheckerList().pipe(
            takeUntil(this.unsubscribe)
        ).subscribe((checkerList: string[] | IServerErrorShortFormat) => {
            if ((checkerList as IServerErrorShortFormat).errorCode) {
            } else {
                this.checkerList = checkerList as string[];
            }
        });
        this.userGroupDataSerivce.retrieve().pipe(
            takeUntil(this.unsubscribe)
        ).subscribe((userGroupList: IUserGroup[] | IServerErrorShortFormat) => {
            if ((userGroupList as IServerErrorShortFormat).errorCode) {
                // (userGroupList as IServerErrorShortFormat).errorMessage;
            } else {
                this.userGroupList = (userGroupList as IUserGroup[]).map((userGroup: IUserGroup) => {
                    return userGroup.id;
                });
            }
        }, (error: IServerErrorFormat) => {

        });
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
        combineLatest(
            this.translateService.get('COMMON.REQUIRED_SELECT'),
            this.translateService.get('CONFIGURATION.COMMON.CHECKER'),
            this.translateService.get('CONFIGURATION.COMMON.USER_GROUP'),
            this.translateService.get('CONFIGURATION.COMMON.THRESHOLD'),
            this.translateService.get('CONFIGURATION.COMMON.TYPE'),
            this.translateService.get('CONFIGURATION.COMMON.NOTES'),
        ).subscribe((i18n: string[]) => {
            this.i18nGuide.CHECKER_REQUIRED = this.translateReplaceService.replace(i18n[0], i18n[1]);
            this.i18nGuide.USER_GROUP_REQUIRED = this.translateReplaceService.replace(i18n[0], i18n[2]);
            this.i18nGuide.THRESHOLD_REQUIRED = this.translateReplaceService.replace(i18n[0], i18n[3]);
            this.i18nGuide.TYPE_REQUIRED = this.translateReplaceService.replace(i18n[0], i18n[4]);

            this.i18nLabel.CHECKER_LABEL = i18n[1];
            this.i18nLabel.USER_GROUP_LABEL = i18n[2];
            this.i18nLabel.THRESHOLD_LABEL = i18n[3];
            this.i18nLabel.TYPE_LABEL = i18n[4];
            this.i18nLabel.NOTES_LABEL = i18n[5];
        });
    }
    private getAlarmData(): void {
        this.showProcessing();
        this.alarmRuleDataService.retrieve(this.currentApplication.getApplicationName()).subscribe((alarmRuleList: IAlarmRule[] | IServerErrorShortFormat) => {
            if ((alarmRuleList as IServerErrorShortFormat).errorCode) {
                this.message = (alarmRuleList as IServerErrorShortFormat).errorMessage;
            } else {
                this.alarmRuleList = alarmRuleList as IAlarmRule[];
            }
            this.hideProcessing();
            this.changeDetectorRef.detectChanges();
        }, (error: IServerErrorFormat) => {
            this.hideProcessing();
            this.message = error.exception.message;
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
            if ((response as IServerErrorShortFormat).errorCode) {
                this.hideProcessing();
                this.message = (response as IServerErrorShortFormat).errorMessage;
            } else {
                this.getAlarmData();
            }
        }, (error: IServerErrorFormat) => {
            this.hideProcessing();
            this.message = error.exception.message;
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
            if ((response as IServerErrorShortFormat).errorCode) {
                this.hideProcessing();
                this.message = (response as IServerErrorShortFormat).errorMessage;
            } else {
                this.getAlarmData();
            }
        }, (error: IServerErrorFormat) => {
            this.hideProcessing();
            this.message = error.exception.message;
        });
    }
    onShowCreateAlarmPopup(): void {
        if (this.isApplicationSelected() === false) {
            return;
        }
        this.showCreate = true;
    }
    onCloseCreateAlarmPopup(): void {
        this.showCreate = false;
    }
    onCloseMessage(): void {
        this.message = '';
    }
    onReload(): void {
        this.getAlarmData();
    }
    onRemoveAlarm(ruleId: string): void {
        this.showProcessing();
        this.alarmRuleDataService.remove(ruleId).subscribe((response: IAlarmRuleResponse | IServerErrorShortFormat) => {
            if ((response as IServerErrorShortFormat).errorCode) {
                this.hideProcessing();
                this.message = (response as IServerErrorShortFormat).errorMessage;
            } else {
                this.getAlarmData();
            }
        }, (error: IServerErrorFormat) => {
            this.hideProcessing();
            this.message = error.exception.message;
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
    }
    hasMessage(): boolean {
        return this.message !== '';
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
