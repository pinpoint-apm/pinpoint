import { Component, OnInit, OnDestroy } from '@angular/core';
import { Subject, combineLatest } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { TranslateService } from '@ngx-translate/core';

import { TranslateReplaceService, WebAppSettingDataService } from 'app/shared/services';
import { GroupMemberInteractionService } from 'app/core/components/group-member/group-member-interaction.service';
import { UserGroupInteractionService } from 'app/core/components/user-group/user-group-interaction.service';
import { PinpointUserInteractionService } from './pinpoint-user-interaction.service';
import { PinpointUser } from './pinpoint-user-create-and-update.component';
import { PinpointUserDataService, IPinpointUser, IPinpointUserResponse } from './pinpoint-user-data.service';

@Component({
    selector: 'pp-pinpoint-user-container',
    templateUrl: './pinpoint-user-container.component.html',
    styleUrls: ['./pinpoint-user-container.component.css']
})
export class PinpointUserContainerComponent implements OnInit, OnDestroy {
    private unsubscribe: Subject<null> = new Subject();
    private searchQuery = '';
    i18nLabel = {
        USER_ID_LABEL: '',
        NAME_LABEL: '',
        DEPARTMENT_LABEL: '',
        PHONE_LABEL: '',
        EMAIL_LABEL: '',
    };
    i18nGuide = {
        USER_ID_MIN_LENGTH: '',
        NAME_MIN_LENGTH: '',
        USER_ID_REQUIRED: '',
        NAME_REQUIRED: '',
        PHONE_REQUIRED: '',
        EMAIL_REQUIRED: '',
    };
    i18nText = {
        SEARCH_INPUT_GUIDE: ''
    };
    minLength = {
        userId: 3,
        name: 3,
        search: 2
    };
    allowedUserEdit = false;
    searchUseEnter = false;
    pinpointUserList: IPinpointUser[] = [];
    filteredPinpointUserList: IPinpointUser[] = [];
    groupMemberList: string[] = [];
    editPinpointUserIndex: number;
    editPinpointUser: PinpointUser;
    isUserGroupSelected = false;
    useDisable = true;
    showLoading = true;
    showCreate = false;
    message = '';

    displayPinpointUserList: IPinpointUser[] = [];
    defaultScrollSize = 100;
    currentSize: number;
    constructor(
        private webAppSettingDataService: WebAppSettingDataService,
        private pinpointUserDataService: PinpointUserDataService,
        private translateService: TranslateService,
        private translateReplaceService: TranslateReplaceService,
        private userGroupInteractionService: UserGroupInteractionService,
        private groupMemberInteractionService: GroupMemberInteractionService,
        private pinpointUserInteractionService: PinpointUserInteractionService
    ) {}
    ngOnInit() {
        this.webAppSettingDataService.useUserEdit().subscribe((allowedUserEdit: boolean) => {
            this.allowedUserEdit = allowedUserEdit;
        });
        this.userGroupInteractionService.onSelect$.pipe(
            takeUntil(this.unsubscribe)
        ).subscribe((userGroupId: string) => {
            this.isUserGroupSelected = userGroupId === '' ? false : true;
        });
        this.groupMemberInteractionService.onChangeGroupMember$.pipe(
            takeUntil(this.unsubscribe)
        ).subscribe((memberList: string[]) => {
            this.groupMemberList = memberList;
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
        combineLatest(
            this.translateService.get('COMMON.MIN_LENGTH'),
            this.translateService.get('COMMON.REQUIRED'),
            this.translateService.get('CONFIGURATION.COMMON.USER_ID'),
            this.translateService.get('CONFIGURATION.COMMON.NAME'),
            this.translateService.get('CONFIGURATION.COMMON.DEPARTMENT'),
            this.translateService.get('CONFIGURATION.COMMON.PHONE'),
            this.translateService.get('CONFIGURATION.COMMON.EMAIL'),
            this.translateService.get('CONFIGURATION.USER_GROUP.USER_GROUP_REQUIRED')
        ).subscribe((i18n: string[]) => {
            this.i18nGuide.USER_ID_MIN_LENGTH = this.translateReplaceService.replace(i18n[0], this.minLength.userId);
            this.i18nGuide.NAME_MIN_LENGTH = this.translateReplaceService.replace(i18n[0], this.minLength.name);
            this.i18nGuide.USER_ID_REQUIRED = this.translateReplaceService.replace(i18n[1], i18n[2]);
            this.i18nGuide.NAME_REQUIRED = this.translateReplaceService.replace(i18n[1], i18n[3]);
            this.i18nGuide.PHONE_REQUIRED = this.translateReplaceService.replace(i18n[1], i18n[5]);
            this.i18nGuide.EMAIL_REQUIRED = this.translateReplaceService.replace(i18n[1], i18n[6]);

            this.i18nText.SEARCH_INPUT_GUIDE = this.translateReplaceService.replace(i18n[0], this.minLength.search);

            this.i18nLabel.USER_ID_LABEL = i18n[2];
            this.i18nLabel.NAME_LABEL = i18n[3];
            this.i18nLabel.DEPARTMENT_LABEL = i18n[4];
            this.i18nLabel.PHONE_LABEL = i18n[5];
            this.i18nLabel.EMAIL_LABEL = i18n[6];
        });
    }
    private getPinpointUserList(): void  {
        this.showProcessing();
        this.webAppSettingDataService.getUserDepartment().subscribe((department: string) => {
            this.pinpointUserDataService.retrieve(department).subscribe((pinpointUserData: IPinpointUser[] | IServerErrorShortFormat) => {
                if ((pinpointUserData as IServerErrorShortFormat).errorCode) {
                    this.message = (pinpointUserData as IServerErrorShortFormat).errorMessage;
                } else {
                    this.pinpointUserList = pinpointUserData as IPinpointUser[];
                    this.filteringPinpointUserList();
                }
                this.hideProcessing();
            }, (error: IServerErrorFormat) => {
                this.message = error.exception.message;
                this.hideProcessing();
            });
        });
    }
    private filteringPinpointUserList(): void {
        if (this.searchQuery === '') {
            this.filteredPinpointUserList = this.pinpointUserList;
        } else {
            this.filteredPinpointUserList = this.pinpointUserList.filter((pinpointUser: IPinpointUser): boolean => {
                return pinpointUser.name.indexOf(this.searchQuery) === -1 ? false : true;
            });
        }
        this.displayPinpointUserList = this.filteredPinpointUserList;
    }
    isEnable(): boolean {
        return false;
    }
    isChecked(userId: string): boolean {
        return this.groupMemberList.indexOf(userId) !== -1;
    }
    hasMessage(): boolean {
        return this.message !== '';
    }
    onAddUser(pinpointUserId: string): void {
        this.showProcessing();
        this.pinpointUserInteractionService.setAddPinpointUser(pinpointUserId);
    }
    onCloseMessage(): void {
        this.message = '';
    }
    onSearch(query: string): void {
        this.searchQuery = query;
        this.filteringPinpointUserList();
    }
    onReload(): void {
        this.getPinpointUserList();
    }
    onCloseCreateUserPopup(): void {
        this.showCreate = false;
    }
    onShowCreateUserPopup(): void {
        this.showCreate = true;
    }
    onCreatePinpointUser(pinpointUser: PinpointUser): void {
        this.pinpointUserDataService.create({
            userId: pinpointUser.userId,
            name: pinpointUser.name,
            phoneNumber: pinpointUser.phoneNumber,
            email: pinpointUser.email,
            department: pinpointUser.department
        } as IPinpointUser).subscribe((response: IPinpointUserResponse | IServerErrorShortFormat) => {
            if ((response as IServerErrorShortFormat).errorCode) {
                this.message = (response as IServerErrorShortFormat).errorMessage;
                this.hideProcessing();
            } else {
                this.getPinpointUserList();
            }
        }, (error: string) => {
            this.hideProcessing();
            this.message = error;
        });
    }
    onUpdatePinpointUser(pinpointUser: PinpointUser): void {
        const editPinpointUser = this.pinpointUserList[this.editPinpointUserIndex];
        this.pinpointUserDataService.update({
            userId: pinpointUser.userId,
            name: pinpointUser.name,
            phoneNumber: pinpointUser.phoneNumber,
            email: pinpointUser.email,
            department: pinpointUser.department,
            number: editPinpointUser.number
        } as IPinpointUser).subscribe((response: IPinpointUserResponse | IServerErrorShortFormat) => {
            if ((response as IServerErrorShortFormat).errorCode) {
                this.message = (response as IServerErrorShortFormat).errorMessage;
                this.hideProcessing();
            } else {
                this.getPinpointUserList();
                this.pinpointUserInteractionService.setUserUpdated({
                    userId: pinpointUser.userId,
                    department: pinpointUser.department,
                    name: pinpointUser.name
                });
            }
        }, (error: IServerErrorFormat) => {
            this.hideProcessing();
            this.message = error.exception.message;
        });
    }
    onRemovePinpointUser(userId: string): void {
        this.showProcessing();
        this.pinpointUserDataService.remove(userId).subscribe((response: IPinpointUserResponse | IServerErrorShortFormat) => {
            if ((response as IServerErrorShortFormat).errorCode) {
                this.message = (response as IServerErrorShortFormat).errorMessage;
            } else {
                this.pinpointUserList.splice(this.getPinpointUserIndexByUserId(userId), 1);
            }
            this.hideProcessing();
        }, (error: IServerErrorFormat) => {
            this.hideProcessing();
            this.message = error.exception.message;
        });
    }
    onEditPinpointUser(userId: string): void {
        this.editPinpointUserIndex = this.getPinpointUserIndexByUserId(userId);
        const editPinpointUser = this.pinpointUserList[this.editPinpointUserIndex];
        this.editPinpointUser = new PinpointUser(
            editPinpointUser.userId,
            editPinpointUser.name,
            editPinpointUser.phoneNumber,
            editPinpointUser.email,
            editPinpointUser.department
        );
        this.onShowCreateUserPopup();
    }
    private getPinpointUserIndexByUserId(userId: string): number {
        let index = -1;
        for (let i = 0 ; i < this.pinpointUserList.length ; i++) {
            if (this.pinpointUserList[i].userId === userId) {
                index = i;
                break;
            }
        }
        return index;
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
