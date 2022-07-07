import { Component, OnInit, OnDestroy, ComponentFactoryResolver, Injector } from '@angular/core';
import { Subject, forkJoin } from 'rxjs';
import { TranslateService } from '@ngx-translate/core';

import { TranslateReplaceService, AnalyticsService, TRACKED_EVENT_LIST, DynamicPopupService, WebAppSettingDataService, WebhookDataService, IWebhook } from 'app/shared/services';
import { UserGroupDataService, IUserGroup } from 'app/core/components/user-group/user-group-data.service';
import { ApplicationListInteractionForConfigurationService } from 'app/core/components/application-list/application-list-interaction-for-configuration.service';
import { NotificationType, IAlarmForm } from './alarm-rule-create-and-update.component';
import { AlarmRuleDataService, IAlarmRule, IAlarmRuleCreated, IAlarmRuleDelete, IAlarmRuleResponse, IAlarmWithWebhook } from './alarm-rule-data.service';
import { isThatType } from 'app/core/utils/util';
import { filter, takeUntil } from 'rxjs/operators';
import { HELP_VIEWER_LIST, HelpViewerPopupContainerComponent } from '../help-viewer-popup/help-viewer-popup-container.component';

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
    webhookList: IWebhook[] = [];
    checkedWebhookList: IWebhook['webhookId'][] = [];
    webhookEnable: boolean;
    showWebhookLoading = false;
    disableWebhookList = false;
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
        private dynamicPopupService: DynamicPopupService,
        private componentFactoryResolver: ComponentFactoryResolver,
        private injector: Injector,
        private webAppSettingDataService: WebAppSettingDataService,
        private webhookDataService: WebhookDataService,
    ) {}

    ngOnInit() {
        this.loadCheckerList();
        this.loadUserGroupList();
        this.bindToAppSelectionEvent();
        this.initI18NText();
        this.initWebhookConfig();
    }

    ngOnDestroy() {
        this.unsubscribe.next();
        this.unsubscribe.complete();
    }

    private initWebhookConfig(): void {
        this.webAppSettingDataService.isWebhookEnable().subscribe((webhookEnable: boolean) => {
            this.webhookEnable = webhookEnable;
        });
    }

    private loadCheckerList(): void {
        this.alarmRuleDataService.getCheckerList().subscribe((result: string[]) => {
            this.checkerList = result as string[];
        }, (error: IServerError) => {
            this.errorMessage = error.message;
        });
    }

    private loadUserGroupList(): void {
        this.userGroupDataSerivce.retrieve().subscribe((result: IUserGroup[]) => {
            this.userGroupList = result.map((userGroup: IUserGroup) => userGroup.id);
        }, (error: IServerError) => {
            this.errorMessage = error.message;
        });
    }

    private bindToAppSelectionEvent(): void {
        this.applicationListInteractionForConfigurationService.onSelectApplication$.pipe(
            takeUntil(this.unsubscribe),
            filter((app: IApplication) => app !== null),
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
        this.alarmRuleDataService.retrieve(this.selectedApplication.getApplicationName()).subscribe((result: IAlarmRule[]) => {
            this.alarmRuleList = result;
            this.hideProcessing();
        }, (error: IServerError) => {
            this.hideProcessing();
            this.errorMessage = error.message;
        });
    }

    private getWebhookList(): void {
        this.checkedWebhookList = [];
        const blockWebhook = (state: boolean) => {
            this.showWebhookLoading = state;
            this.disableWebhookList = state;
        };

        blockWebhook(true);

        if (this.editAlarm) {
            forkJoin(
                this.webhookDataService.getWebhookListByAppId(this.selectedApplication.applicationName),
                this.webhookDataService.getWebhookListByAlarmId(this.editAlarm.ruleId),
            ).subscribe(([webhookList, checkedWebhookList]: IWebhook[][]) => {
                this.webhookList = webhookList;
                this.checkedWebhookList = checkedWebhookList.map(({webhookId}) => webhookId);

                blockWebhook(false);
            }, (error: IServerError) => {
                this.errorMessage = error.message;
                blockWebhook(false);
            });
        } else {
            this.webhookDataService.getWebhookListByAppId(this.selectedApplication.applicationName).subscribe((result: IWebhook[]) => {
                this.webhookList = result;

                blockWebhook(false);
            }, (error: IServerError) => {
                this.errorMessage = error.message;
                blockWebhook(false);
            });
        }
      }

    onCreateAlarm({checkerName, userGroupId, threshold, type, notes}: IAlarmForm): void {
       this.showProcessing();
        const alarmRule = {
            applicationId: this.selectedApplication.getApplicationName(),
            serviceType: this.selectedApplication.getServiceType(),
            checkerName,
            userGroupId,
            threshold,
            emailSend: type === NotificationType.ALL || type === NotificationType.EMAIL,
            smsSend: type === NotificationType.ALL || type === NotificationType.SMS,
            webhookSend: (this.webhookEnable && type === NotificationType.ALL) || type === NotificationType.WEBHOOK,
            notes
        };
        const isWithWebhook = this.webhookEnable && (type === 'all' || type === 'webhook') && this.checkedWebhookList.length > 0;
        const param = isWithWebhook
            ? {rule: alarmRule, webhookIds: this.checkedWebhookList}
            : alarmRule;
        const postAlarmRule = isWithWebhook
            ? this.alarmRuleDataService.createWithWebhook(param as IAlarmWithWebhook)
            : this.alarmRuleDataService.create(param);

        postAlarmRule.subscribe((response: IAlarmRuleCreated) => {
            this.getAlarmData();
            this.analyticsService.trackEvent(TRACKED_EVENT_LIST.CREATE_ALARM);
        }, (error: IServerError) => {
            this.hideProcessing();
            this.errorMessage = error.message;
        });
    }

    onUpdateAlarm({checkerName, userGroupId, threshold, type, notes}: IAlarmForm): void {
        this.showProcessing();
        const {ruleId, applicationId, serviceType} = this.editAlarm;
        const alarmRule = {
            ruleId,
            applicationId,
            serviceType,
            checkerName,
            userGroupId,
            threshold,
            emailSend: type === NotificationType.ALL || type === NotificationType.EMAIL,
            smsSend: type === NotificationType.ALL || type === NotificationType.SMS,
            webhookSend: (this.webhookEnable && type === NotificationType.ALL) || type === NotificationType.WEBHOOK,
            notes,
        };

        const isWithWebhook = this.webhookEnable && (type === 'all' || type === 'webhook');
        const param = isWithWebhook
            ? {rule: alarmRule, webhookIds: this.checkedWebhookList}
            : alarmRule;
        const putAlarmRule = isWithWebhook
            ? this.alarmRuleDataService.updateWithWebhook(param as IAlarmWithWebhook)
            : this.alarmRuleDataService.update(param as IAlarmRule);

        putAlarmRule.subscribe((response: IAlarmRuleResponse) => {
            this.getAlarmData();
            this.analyticsService.trackEvent(TRACKED_EVENT_LIST.UPDATE_ALARM);
        }, (error: IServerError) => {
            this.hideProcessing();
            this.errorMessage = error.message;
        });
    }

    onClickAddBtn(): void {
        this.editAlarm = null;
        this.showPopup = true;
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.SHOW_ALARM_CREATION_POPUP);
        if (this.webhookEnable) {
            this.getWebhookList();
        }
    }

    onClosePopup(): void {
        this.showPopup = false;
    }

    onCloseErrorMessage(): void {
        this.errorMessage = '';
    }

    onRemoveAlarm({ruleId, emailSend, smsSend, webhookSend}: IAlarmRule): void {
        this.showProcessing();
        const params: IAlarmRuleDelete = {
            ruleId,
            emailSend,
            smsSend,
            webhookSend,
            applicationId: this.selectedApplication.getApplicationName(),
        };

        this.alarmRuleDataService.remove(params).subscribe((response: IAlarmRuleResponse) => {
            this.getAlarmData();
            this.analyticsService.trackEvent(TRACKED_EVENT_LIST.REMOVE_ALARM);
        }, (error: IServerError) => {
            this.hideProcessing();
            this.errorMessage = error.message;
        });
    }

    onEditAlarm(ruleId: string): void {
        this.editAlarm = this.alarmRuleList.find(({ruleId: alarmId}: IAlarmRule) => alarmId === ruleId);
        this.showPopup = true;
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.SHOW_ALARM_UPDATE_POPUP);
        if (this.webhookEnable) {
            this.getWebhookList();
        }
    }

    onCheckWebhook(webhookId: IWebhook['webhookId']): void {
        if (this.checkedWebhookList.some((wId: string) => wId === webhookId)) {
            this.checkedWebhookList = this.checkedWebhookList.filter((wId: string) => !(wId === webhookId));
        } else {
            this.checkedWebhookList.push(webhookId);
        }
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

    onShowHelp({coord}: {coord: ICoordinate}): void {
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.TOGGLE_HELP_VIEWER, HELP_VIEWER_LIST.ALARM);

        this.dynamicPopupService.openPopup({
            data: HELP_VIEWER_LIST.ALARM,
            coord,
            component: HelpViewerPopupContainerComponent
        }, {
            resolver: this.componentFactoryResolver,
            injector: this.injector
        });
    }
}
