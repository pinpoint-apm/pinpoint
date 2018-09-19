import { Component, OnInit, OnDestroy } from '@angular/core';
import { Subject, combineLatest } from 'rxjs';
import { TranslateService } from '@ngx-translate/core';
import { WebAppSettingDataService, TranslateReplaceService } from 'app/shared/services';
import { UserGroupInteractionService } from './user-group-interaction.service';
import { UserGroupDataService, IUserGroup, IUserGroupCreated, IUserGroupDeleted } from './user-group-data.service';

@Component({
    selector: 'pp-user-group-container',
    templateUrl: './user-group-container.component.html',
    styleUrls: ['./user-group-container.component.css']
})
export class UserGroupContainerComponent implements OnInit, OnDestroy {
    private unsubscribe: Subject<null> = new Subject();
    private searchQuery = '';
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
    message = '';
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
            this.getUserGroupList(userId === '' ? null : {
                userId: userId
            });
        });
    }
    ngOnDestroy() {
        this.unsubscribe.next();
        this.unsubscribe.complete();
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
    private getUserGroupList(param: any): void  {
        this.userGroupDataService.retrieve(param).subscribe((userGroupData: IUserGroup[]) => {
            this.userGroupList = userGroupData;
            this.hideProcessing();
        }, (error: string) => {
            this.hideProcessing();
            this.message = error;
        });
    }
    private makeUserGroupQuery(userId: string): any {
        return this.searchQuery === '' ? {
            userId: userId
        } : {
            userGroupId: this.searchQuery
        };
    }
    onRemoveUserGroup(id: string): void {
        this.showProcessing();
        this.webAppSettingDataService.getUserId().subscribe((userId: string = '') => {
            this.userGroupDataService.remove(id, userId).subscribe((response: IUserGroupDeleted) => {
                if (response.result === 'SUCCESS') {
                    this.userGroupInteractionService.setSelectedUserGroup('');
                    this.getUserGroupList(this.makeUserGroupQuery(userId));
                } else {
                    this.hideProcessing();
                }
            }, (error: string) => {
                this.hideProcessing();
                this.message = error;
            });
        });
    }
    onCreateUserGroup(newUserGroupName: string): void {
        this.showProcessing();
        this.webAppSettingDataService.getUserId().subscribe((userId: string = '') => {
            this.userGroupDataService.create(newUserGroupName, userId).subscribe((userGroupData: IUserGroupCreated) => {
                this.userGroupList.push({
                    id: newUserGroupName,
                    number: userGroupData.number
                });
                this.hideProcessing();
            }, (error: string) => {
                this.hideProcessing();
                this.message = error;
            });
        });
    }
    onCloseCreateUserPopup(): void {
        this.showCreate = false;
    }
    onShowCreateUserPopup(): void {
        this.showCreate = true;
    }
    hasMessage(): boolean {
        return this.message !== '';
    }
    onSelectUserGroup(userGroupId: string): void {
        this.selectedUserGroupId = userGroupId;
        this.userGroupInteractionService.setSelectedUserGroup(userGroupId);
    }
    onCloseMessage(): void {
        this.message = '';
    }
    onSearch(query: string): void {
        this.searchQuery = query;
        this.webAppSettingDataService.getUserId().subscribe((userId: string = '') => {
            this.getUserGroupList(this.makeUserGroupQuery(userId));
        });
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
