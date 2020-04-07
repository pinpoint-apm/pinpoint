import { Component, OnInit, OnDestroy } from '@angular/core';
import { Subject, forkJoin } from 'rxjs';
import { TranslateService } from '@ngx-translate/core';

import { TranslateReplaceService, WebAppSettingDataService, MessageQueueService, MESSAGE_TO, AnalyticsService, TRACKED_EVENT_LIST } from 'app/shared/services';
import { PinpointUserDataService, IPinpointUserResponse } from './pinpoint-user-data.service';
import { isThatType, isEmpty } from 'app/core/utils/util';

@Component({
    selector: 'pp-pinpoint-user-container',
    templateUrl: './pinpoint-user-container.component.html',
    styleUrls: ['./pinpoint-user-container.component.css']
})
export class PinpointUserContainerComponent implements OnInit, OnDestroy {
    private unsubscribe = new Subject<void>();
    private searchQuery = '';

    i18nLabel = {
        USER_ID_LABEL: '',
        NAME_LABEL: '',
        DEPARTMENT_LABEL: '',
        PHONE_LABEL: '',
        EMAIL_LABEL: '',
    };
    i18nGuide: { [key: string]: IFormFieldErrorType };
    i18nText = {
        EMPTY: '',
        SEARCH_INPUT_GUIDE: ''
    };
    minLength = {
        userId: 3,
        name: 3,
        search: 2
    };
    allowedUserEdit = false;
    searchUseEnter = true;
    pinpointUserList: IUserProfile[] = [];
    groupMemberList: string[] = [];
    userInfo: IUserProfile;
    isUserGroupSelected = false;
    useDisable = true;
    showLoading = true;
    showCreate = false;
    errorMessage: string;
    isEmpty: boolean;

    constructor(
        private webAppSettingDataService: WebAppSettingDataService,
        private pinpointUserDataService: PinpointUserDataService,
        private translateService: TranslateService,
        private translateReplaceService: TranslateReplaceService,
        private messageQueueService: MessageQueueService,
        private analyticsService: AnalyticsService,
    ) {}

    ngOnInit() {
        this.webAppSettingDataService.useUserEdit().subscribe((allowedUserEdit: boolean) => {
            this.allowedUserEdit = allowedUserEdit;
        });
        this.messageQueueService.receiveMessage(this.unsubscribe, MESSAGE_TO.USER_GROUP_SELECTED_USER_GROUP).subscribe((userGroupId: string) => {
            this.isUserGroupSelected = userGroupId === '' ? false : true;
        });
        this.messageQueueService.receiveMessage(this.unsubscribe, MESSAGE_TO.GROUP_MEMBER_SET_CURRENT_GROUP_MEMBERS).subscribe((param: string[]) => {
            this.groupMemberList = param;
            this.hideProcessing();
        });
        this.getI18NText();
        this.getPinpointUserList();
    }

    ngOnDestroy() {
        this.unsubscribe.next();
        this.unsubscribe.complete();
    }

    private getI18NText(): void {
        forkJoin(
            this.translateService.get('COMMON.EMPTY_ON_SEARCH'),
            this.translateService.get('COMMON.MIN_LENGTH'),
            this.translateService.get('COMMON.REQUIRED'),
            this.translateService.get('CONFIGURATION.COMMON.USER_ID'),
            this.translateService.get('CONFIGURATION.COMMON.NAME'),
            this.translateService.get('CONFIGURATION.COMMON.DEPARTMENT'),
            this.translateService.get('CONFIGURATION.COMMON.PHONE'),
            this.translateService.get('CONFIGURATION.COMMON.EMAIL'),
            this.translateService.get('CONFIGURATION.PINPOINT_USER.USER_ID_VALIDATION'),
            this.translateService.get('CONFIGURATION.PINPOINT_USER.NAME_VALIDATION'),
            this.translateService.get('CONFIGURATION.PINPOINT_USER.DEPARTMENT_VALIDATION'),
            this.translateService.get('CONFIGURATION.PINPOINT_USER.PHONE_VALIDATION'),
            this.translateService.get('CONFIGURATION.PINPOINT_USER.EMAIL_VALIDATION'),
        ).subscribe(([
            emptyText, minLengthMessage, requiredMessage, idLabel, nameLabel, departmentLabel, phoneLabel, emailLabel,
            userIdValidation, nameValidation, departmentValidation, phoneValidation, emailValidation
        ]: string[]) => {
            this.i18nGuide = {
                userId: {
                    required: this.translateReplaceService.replace(requiredMessage, idLabel),
                    valueRule: userIdValidation
                },
                name: {
                    required: this.translateReplaceService.replace(requiredMessage, nameLabel),
                    valueRule: nameValidation
                },
                department: {
                    valueRule: departmentValidation
                },
                phoneNumber: {
                    valueRule: phoneValidation
                },
                email: {
                    minlength: emailValidation,
                    maxlength: emailValidation,
                    valueRule: emailValidation
                }
            };
            this.i18nText.EMPTY = emptyText;
            this.i18nText.SEARCH_INPUT_GUIDE = this.translateReplaceService.replace(minLengthMessage, this.minLength.search);

            this.i18nLabel.USER_ID_LABEL = idLabel;
            this.i18nLabel.NAME_LABEL = nameLabel;
            this.i18nLabel.DEPARTMENT_LABEL = departmentLabel;
            this.i18nLabel.PHONE_LABEL = phoneLabel;
            this.i18nLabel.EMAIL_LABEL = emailLabel;
        });
    }

    private getPinpointUserList(query?: string): void  {
        this.showProcessing();
        this.pinpointUserDataService.retrieve(query).subscribe((result: IUserProfile[] | IServerErrorShortFormat) => {
            isThatType<IServerErrorShortFormat>(result, 'errorCode', 'errorMessage')
                ? this.errorMessage = result.errorMessage
                : (this.pinpointUserList = result, this.isEmpty = isEmpty(this.pinpointUserList));
            this.hideProcessing();
        }, (error: IServerErrorFormat) => {
            this.errorMessage = error.exception.message;
            this.hideProcessing();
        });
    }

    isChecked(userId: string): boolean {
        return this.groupMemberList.indexOf(userId) !== -1;
    }

    onAddUser(pinpointUserId: string): void {
        this.messageQueueService.sendMessage({
            to: MESSAGE_TO.PINPOINT_USER_ADD_USER,
            param: pinpointUserId
        });
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.ADD_USER_TO_GROUP);
    }

    onCloseErrorMessage(): void {
        this.errorMessage = '';
    }

    onSearch(query: string): void {
        this.searchQuery = query;
        this.getPinpointUserList(this.searchQuery);
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.SEARCH_USER);
    }

    onReload(): void {
        this.getPinpointUserList(this.searchQuery);
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.RELOAD_USER_LIST);
    }

    onCloseCreateUserPopup(): void {
        this.showCreate = false;
    }

    onShowAddUser(): void {
        this.userInfo = null;
        this.showCreate = true;
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.SHOW_USER_CREATION_POPUP);
    }

    // TODO: Refactor - Avoid nested subscribe syntax.
    onCreatePinpointUser(userInfo: IUserProfile): void {
        this.pinpointUserDataService.create(userInfo).subscribe((response: IPinpointUserResponse | IServerErrorShortFormat) => {
            isThatType<IServerErrorShortFormat>(response, 'errorCode', 'errorMessage')
                ? this.errorMessage = response.errorMessage
                : (
                    this.getPinpointUserList(this.searchQuery),
                    this.analyticsService.trackEvent(TRACKED_EVENT_LIST.CREATE_USER)
                );
            this.hideProcessing();
        }, (error: string) => {
            this.hideProcessing();
            this.errorMessage = error;
        });
    }

    // TODO: Refactor - Avoid nested subscribe syntax.
    onUpdatePinpointUser(userInfo: IUserProfile): void {
        this.showProcessing();
        this.pinpointUserDataService.update(userInfo).subscribe((response: IPinpointUserResponse | IServerErrorShortFormat) => {
            if (isThatType<IServerErrorShortFormat>(response, 'errorCode', 'errorMessage')) {
                this.errorMessage = response.errorMessage;
            } else {
                this.getPinpointUserList(this.searchQuery);
                if (this.isUserGroupSelected) {
                    this.messageQueueService.sendMessage({
                        to: MESSAGE_TO.PINPOINT_USER_UPDATE_USER,
                        param: {
                            userId: userInfo.userId,
                            department: userInfo.department,
                            name: userInfo.name
                        }
                    });
                }
                this.analyticsService.trackEvent(TRACKED_EVENT_LIST.UPDATE_USER);
            }

            this.hideProcessing();
        }, (error: IServerErrorFormat) => {
            this.hideProcessing();
            this.errorMessage = error.exception.message;
        });
    }

    // TODO: Refactor - Avoid nested subscribe syntax.
    onRemovePinpointUser(userId: string): void {
        this.showProcessing();
        this.pinpointUserDataService.remove(userId).subscribe((response: IPinpointUserResponse | IServerErrorShortFormat) => {
            if (isThatType<IServerErrorShortFormat>(response, 'errorCode', 'errorMessage')) {
                this.errorMessage = response.errorMessage;
            } else {
                this.getPinpointUserList(this.searchQuery);
                if (this.isUserGroupSelected) {
                    this.messageQueueService.sendMessage({
                        to: MESSAGE_TO.PINPOINT_USER_REMOVE_USER,
                        param: userId
                    });
                }
                this.analyticsService.trackEvent(TRACKED_EVENT_LIST.REMOVE_USER);
            }
            this.hideProcessing();
        }, (error: IServerErrorFormat) => {
            this.hideProcessing();
            this.errorMessage = error.exception.message;
        });
    }

    onShowUpdateUser(userId: string): void {
        this.userInfo = this.getUserInfo(userId);
        this.showCreate = true;
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.SHOW_USER_UPDATE_POPUP);
    }

    private getUserInfo(userId: string): IUserProfile {
        return this.pinpointUserList.find(({userId: id}: IUserProfile) => id === userId);
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
