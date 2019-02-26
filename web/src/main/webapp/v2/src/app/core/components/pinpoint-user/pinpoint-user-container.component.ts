import { Component, OnInit, OnDestroy } from '@angular/core';
import { Subject, iif, of, forkJoin } from 'rxjs';
import { switchMap } from 'rxjs/operators';
import { TranslateService } from '@ngx-translate/core';

import { TranslateReplaceService, WebAppSettingDataService, MessageQueueService, MESSAGE_TO } from 'app/shared/services';
import { PinpointUser } from './pinpoint-user-create-and-update.component';
import { PinpointUserDataService, IPinpointUser, IPinpointUserResponse } from './pinpoint-user-data.service';
import { isThatType } from 'app/core/utils/util';

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
    i18nGuide: { [key: string]: IFormFieldErrorType };
    i18nText = {
        SEARCH_INPUT_GUIDE: ''
    };
    minLength = {
        userId: 3,
        name: 3,
        search: 2
    };
    allowedUserEdit = false;
    searchUseEnter = true;
    pinpointUserList: IPinpointUser[] = [];
    groupMemberList: string[] = [];
    editPinpointUserIndex: number;
    editPinpointUser: PinpointUser;
    isUserGroupSelected = false;
    useDisable = true;
    showLoading = true;
    showCreate = false;
    errorMessage: string;

    constructor(
        private webAppSettingDataService: WebAppSettingDataService,
        private pinpointUserDataService: PinpointUserDataService,
        private translateService: TranslateService,
        private translateReplaceService: TranslateReplaceService,
        private messageQueueService: MessageQueueService
    ) {}
    ngOnInit() {
        this.webAppSettingDataService.useUserEdit().subscribe((allowedUserEdit: boolean) => {
            this.allowedUserEdit = allowedUserEdit;
        });
        this.messageQueueService.receiveMessage(this.unsubscribe, MESSAGE_TO.USER_GROUP_SELECTED_USER_GROUP).subscribe((param: any[]) => {
            const userGroupId = param[0];
            this.isUserGroupSelected = userGroupId === '' ? false : true;
        });
        this.messageQueueService.receiveMessage(this.unsubscribe, MESSAGE_TO.GROUP_MEMBER_SET_CURRENT_GROUP_MEMBERS).subscribe((param: any[]) => {
            this.groupMemberList = param[0] as string[];
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
            this.translateService.get('COMMON.MIN_LENGTH'),
            this.translateService.get('COMMON.REQUIRED'),
            this.translateService.get('CONFIGURATION.COMMON.USER_ID'),
            this.translateService.get('CONFIGURATION.COMMON.NAME'),
            this.translateService.get('CONFIGURATION.COMMON.DEPARTMENT'),
            this.translateService.get('CONFIGURATION.COMMON.PHONE'),
            this.translateService.get('CONFIGURATION.COMMON.EMAIL'),
        ).subscribe(([minLengthMessage, requiredMessage, idLabel, nameLabel, departmentLabel, phoneLabel, emailLabel]: string[]) => {
            this.i18nGuide = {
                userId: {
                    required: this.translateReplaceService.replace(requiredMessage, idLabel),
                    minlength: this.translateReplaceService.replace(minLengthMessage, this.minLength.userId)
                },
                name: {
                    required: this.translateReplaceService.replace(requiredMessage, nameLabel),
                    minlength: this.translateReplaceService.replace(minLengthMessage, this.minLength.name)
                },
                phoneNumber: { required: this.translateReplaceService.replace(requiredMessage, phoneLabel) },
                email: { required: this.translateReplaceService.replace(requiredMessage, emailLabel) }
            };

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
        iif(() => !!query,
            of(query),
            this.webAppSettingDataService.getUserDepartment()
        ).pipe(
            switchMap((department?: string) => this.pinpointUserDataService.retrieve(department))
        ).subscribe((result: IPinpointUser[] | IServerErrorShortFormat) => {
            isThatType<IServerErrorShortFormat>(result, 'errorCode', 'errorMessage')
                ? this.errorMessage = result.errorMessage
                : this.pinpointUserList = result;
            this.hideProcessing();
        }, (error: IServerErrorFormat) => {
            this.errorMessage = error.exception.message;
            this.hideProcessing();
        });
    }
    isEnable(): boolean {
        return false;
    }
    isChecked(userId: string): boolean {
        return this.groupMemberList.indexOf(userId) !== -1;
    }
    onAddUser(pinpointUserId: string): void {
        this.messageQueueService.sendMessage({
            to: MESSAGE_TO.PINPOINT_USER_ADD_USER,
            param: [pinpointUserId]
        });
    }
    onCloseErrorMessage(): void {
        this.errorMessage = '';
    }
    onSearch(query: string): void {
        this.searchQuery = query;
        this.getPinpointUserList(this.searchQuery);
    }
    onReload(): void {
        this.getPinpointUserList(this.searchQuery);
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
            isThatType<IServerErrorShortFormat>(response, 'errorCode', 'errorMessage')
                ? this.errorMessage = response.errorMessage
                : this.getPinpointUserList(this.searchQuery);
            this.hideProcessing();
        }, (error: string) => {
            this.hideProcessing();
            this.errorMessage = error;
        });
    }
    onUpdatePinpointUser(pinpointUser: PinpointUser): void {
        this.showProcessing();
        const editPinpointUser = this.pinpointUserList[this.editPinpointUserIndex];
        this.pinpointUserDataService.update({
            userId: pinpointUser.userId,
            name: pinpointUser.name,
            phoneNumber: pinpointUser.phoneNumber,
            email: pinpointUser.email,
            department: pinpointUser.department,
            number: editPinpointUser.number
        } as IPinpointUser).subscribe((response: IPinpointUserResponse | IServerErrorShortFormat) => {
            if (isThatType<IServerErrorShortFormat>(response, 'errorCode', 'errorMessage')) {
                this.errorMessage = response.errorMessage;
            } else {
                this.getPinpointUserList(this.searchQuery);
                this.messageQueueService.sendMessage({
                    to: MESSAGE_TO.PINPOINT_USER_UPDATE_USER,
                    param: [{
                        userId: pinpointUser.userId,
                        department: pinpointUser.department,
                        name: pinpointUser.name
                    }]
                });
            }

            this.hideProcessing();
        }, (error: IServerErrorFormat) => {
            this.hideProcessing();
            this.errorMessage = error.exception.message;
        });
    }
    onRemovePinpointUser(userId: string): void {
        this.showProcessing();
        this.pinpointUserDataService.remove(userId).subscribe((response: IPinpointUserResponse | IServerErrorShortFormat) => {
            if (isThatType<IServerErrorShortFormat>(response, 'errorCode', 'errorMessage')) {
                this.errorMessage = response.errorMessage;
            } else {
                this.pinpointUserList.splice(this.getPinpointUserIndexByUserId(userId), 1);
                this.messageQueueService.sendMessage({
                    to: MESSAGE_TO.PINPOINT_USER_REMOVE_USER,
                    param: [userId]
                });
            }
            this.hideProcessing();
        }, (error: IServerErrorFormat) => {
            this.hideProcessing();
            this.errorMessage = error.exception.message;
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
