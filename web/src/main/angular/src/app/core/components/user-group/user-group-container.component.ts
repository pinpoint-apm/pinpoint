import { Component, OnInit } from '@angular/core';
import { forkJoin } from 'rxjs';
import { TranslateService } from '@ngx-translate/core';

import { WebAppSettingDataService, TranslateReplaceService, MessageQueueService, MESSAGE_TO, AnalyticsService, TRACKED_EVENT_LIST } from 'app/shared/services';
import { UserGroupDataService, IUserGroup, IUserGroupCreated, IUserGroupDeleted } from './user-group-data.service';
import { isThatType, isEmpty } from 'app/core/utils/util';

@Component({
    selector: 'pp-user-group-container',
    templateUrl: './user-group-container.component.html',
    styleUrls: ['./user-group-container.component.css']
})
export class UserGroupContainerComponent implements OnInit {
    private searchQuery = '';
    private userId = '';
    i18nLabel = {
        NAME_LABEL: '',
    };
    i18nGuide: { [key: string]: IFormFieldErrorType };
    i18nText = {
        SEARCH_INPUT_GUIDE: '',
        EMPTY: ''
    };
    USER_GROUP_NAME_MIN_LENGTH = 4;
    SEARCH_MIN_LENGTH = 2;
    searchUseEnter = true;
    userGroupList: IUserGroup[] = [];
    useDisable = true;
    showLoading = true;
    showCreate = false;
    errorMessage: string;
    selectedUserGroupId = '';
    isEmpty: boolean;

    constructor(
        private webAppSettingDataService: WebAppSettingDataService,
        private translateService: TranslateService,
        private translateReplaceService: TranslateReplaceService,
        private messageQueueService: MessageQueueService,
        private userGroupDataService: UserGroupDataService,
        private analyticsService: AnalyticsService,
    ) {}

    ngOnInit() {
        this.getI18NText();
        this.webAppSettingDataService.getUserId().subscribe((userId: string = '') => {
            this.userId = userId;
            this.getUserGroupList({userId: this.userId});
        });
    }

    private getI18NText(): void {
        forkJoin(
            this.translateService.get('COMMON.MIN_LENGTH'),
            this.translateService.get('COMMON.REQUIRED'),
            this.translateService.get('CONFIGURATION.COMMON.NAME'),
            this.translateService.get('CONFIGURATION.USER_GROUP.VALIDATION'),
            this.translateService.get('COMMON.EMPTY_ON_SEARCH')
        ).subscribe(([minLengthMessage, requiredMessage, nameLabel, validationGuide, emptyText]: string[]) => {
            this.i18nGuide = {
                userGroupName: {
                    required: this.translateReplaceService.replace(requiredMessage, nameLabel),
                    minlength: this.translateReplaceService.replace(minLengthMessage, this.USER_GROUP_NAME_MIN_LENGTH),
                    valueRule: validationGuide
                }
            };

            this.i18nText.SEARCH_INPUT_GUIDE = this.translateReplaceService.replace(minLengthMessage, this.SEARCH_MIN_LENGTH);
            this.i18nText.EMPTY = emptyText;
            this.i18nLabel.NAME_LABEL = nameLabel;
        });
    }

    private getUserGroupList(params: any): void  {
        this.userGroupDataService.retrieve(params).subscribe((data: IUserGroup[] | IServerErrorShortFormat) => {
            isThatType<IServerErrorShortFormat>(data, 'errorCode', 'errorMessage')
                ? this.errorMessage = data.errorMessage
                : (this.userGroupList = data, this.isEmpty = isEmpty(this.userGroupList));
            this.hideProcessing();
        }, (error: IServerErrorFormat) => {
            this.hideProcessing();
            this.errorMessage = error.exception.message;
        });
    }

    private makeUserGroupQuery(): any {
        return this.searchQuery === '' ? {userId: this.userId} : {userGroupId: this.searchQuery};
    }

    onRemoveUserGroup(id: string): void {
        this.showProcessing();
        this.userGroupDataService.remove(id, this.userId).subscribe((response: IUserGroupDeleted | IServerErrorShortFormat) => {
            if (isThatType<IServerErrorShortFormat>(response, 'errorCode', 'errorMessage')) {
                this.errorMessage = response.errorMessage;
                this.hideProcessing();
            } else {
                if (response.result === 'SUCCESS') {
                    this.messageQueueService.sendMessage({
                        to: MESSAGE_TO.USER_GROUP_SELECTED_USER_GROUP,
                        param: ''
                    });
                    this.getUserGroupList(this.makeUserGroupQuery());
                    this.analyticsService.trackEvent(TRACKED_EVENT_LIST.REMOVE_USER_GROUP);
                } else {
                    this.hideProcessing();
                }
            }
        }, (error: IServerErrorFormat) => {
            this.hideProcessing();
            this.errorMessage = error.exception.message;
        });
    }

    onCreateUserGroup(newUserGroupName: string): void {
        this.showProcessing();
        this.userGroupDataService.create(newUserGroupName, this.userId).subscribe((data: IUserGroupCreated | IServerErrorShortFormat) => {
            if (isThatType<IServerErrorShortFormat>(data, 'errorCode', 'errorMessage')) {
                this.errorMessage = data.errorMessage;
            } else {
                this.userGroupList.push({
                    id: newUserGroupName,
                    number: data.number
                });
                this.analyticsService.trackEvent(TRACKED_EVENT_LIST.CREATE_USER_GROUP);
            }
            this.hideProcessing();
        }, (error: IServerErrorFormat) => {
            this.hideProcessing();
            this.errorMessage = error.exception.message;
        });
    }

    onCloseCreateUserPopup(): void {
        this.showCreate = false;
    }

    onShowCreateUserPopup(): void {
        this.showCreate = true;
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.SHOW_USER_GROUP_CREATION_POPUP);
    }

    onSelectUserGroup(userGroupId: string): void {
        this.selectedUserGroupId = userGroupId;
        this.messageQueueService.sendMessage({
            to: MESSAGE_TO.USER_GROUP_SELECTED_USER_GROUP,
            param: userGroupId
        });
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.SELECT_USER_GROUP);
    }

    onCloseErrorMessage(): void {
        this.errorMessage = '';
    }

    onReload(): void {
        this.showProcessing();
        this.getUserGroupList(this.makeUserGroupQuery());
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.RELOAD_USER_GROUP_LIST);
    }

    onSearch(query: string): void {
        this.showProcessing();
        this.searchQuery = query;
        this.getUserGroupList(this.makeUserGroupQuery());
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.SEARCH_USER_GROUP);
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
