import { Component, OnInit } from '@angular/core';
import { combineLatest } from 'rxjs';
import { TranslateService } from '@ngx-translate/core';
import { WebAppSettingDataService, TranslateReplaceService } from 'app/shared/services';
import { UserGroupInteractionService } from './user-group-interaction.service';
import { UserGroupDataService, IUserGroup, IUserGroupCreated, IUserGroupDeleted } from './user-group-data.service';
import { isThatType } from 'app/core/utils/util';

@Component({
    selector: 'pp-user-group-container',
    templateUrl: './user-group-container.component.html',
    styleUrls: ['./user-group-container.component.css']
})
export class UserGroupContainerComponent implements OnInit {
    private searchQuery = '';
    private userId = '';
    i18nText: { [key: string]: string } = {
        NAME_LABEL: '',
        USER_GROUP_NAME_REQUIRED: '',
        USER_GROUP_NAME_MIN_LENGTH: '',
        USER_GROUP_SERACH_MIN_LENGTH: ''
    };
    USER_GROUP_NAME_MIN_LENGTH = 3;
    SEARCH_MIN_LENGTH = 2;
    searchUseEnter = false;
    userGroupList: IUserGroup[] = [];
    useDisable = true;
    showLoading = true;
    showCreate = false;
    errorMessage: string;
    selectedUserGroupId = '';
    constructor(
        private webAppSettingDataService: WebAppSettingDataService,
        private userGroupDataService: UserGroupDataService,
        private translateService: TranslateService,
        private translateReplaceService: TranslateReplaceService,
        private userGroupInteractionService: UserGroupInteractionService
    ) {}
    ngOnInit() {
        this.getI18NText();
        this.webAppSettingDataService.getUserId().subscribe((userId: string = '') => {
            this.userId = userId;
            this.getUserGroupList({ userId: this.userId});
        });
    }
    private getI18NText(): void {
        combineLatest(
            this.translateService.get('COMMON.MIN_LENGTH'),
            this.translateService.get('COMMON.REQUIRED'),
            this.translateService.get('CONFIGURATION.COMMON.NAME')
        ).subscribe((i18n: string[]) => {
            this.i18nText.USER_GROUP_NAME_MIN_LENGTH = this.translateReplaceService.replace(i18n[0], this.USER_GROUP_NAME_MIN_LENGTH);
            this.i18nText.USER_GROUP_SEARCH_MIN_LENGTH = this.translateReplaceService.replace(i18n[0], this.SEARCH_MIN_LENGTH);
            this.i18nText.USER_GROUP_NAME_REQUIRED = this.translateReplaceService.replace(i18n[1], i18n[2]);
            this.i18nText.NAME_LABEL = i18n[2];
        });
    }
    private getUserGroupList(params: any): void  {
        this.userGroupDataService.retrieve(params).subscribe((data: IUserGroup[] | IServerErrorShortFormat) => {
            isThatType<IServerErrorShortFormat>(data, 'errorCode', 'errorMessage')
                ? this.errorMessage = data.errorMessage
                : this.userGroupList = data;
            this.hideProcessing();
        }, (error: IServerErrorFormat) => {
            this.hideProcessing();
            this.errorMessage = error.exception.message;
        });
    }
    private makeUserGroupQuery(): any {
        return this.searchQuery === '' ? {
            userId: this.userId
        } : {
            userGroupId: this.searchQuery
        };
    }
    onRemoveUserGroup(id: string): void {
        this.showProcessing();
        this.userGroupDataService.remove(id, this.userId).subscribe((response: IUserGroupDeleted | IServerErrorShortFormat) => {
            if (isThatType<IServerErrorShortFormat>(response, 'errorCode', 'errorMessage')) {
                this.errorMessage = response.errorMessage;
                this.hideProcessing();
            } else {
                if (response.result === 'SUCCESS') {
                    this.userGroupInteractionService.setSelectedUserGroup('');
                    this.getUserGroupList(this.makeUserGroupQuery());
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
    }
    onSelectUserGroup(userGroupId: string): void {
        this.selectedUserGroupId = userGroupId;
        this.userGroupInteractionService.setSelectedUserGroup(userGroupId);
    }
    onCloseErrorMessage(): void {
        this.errorMessage = '';
    }
    onReload(): void {
        this.showProcessing();
        this.getUserGroupList(this.makeUserGroupQuery());
    }
    onSearch(query: string): void {
        this.showProcessing();
        this.searchQuery = query;
        this.getUserGroupList(this.makeUserGroupQuery());
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
