import { Component, OnInit, OnDestroy } from '@angular/core';
import { Subject, Observable } from 'rxjs';
import { TranslateService } from '@ngx-translate/core';

import { GroupMemberDataService, IGroupMember, IGroupMemberResponse } from './group-member-data.service';
import { isThatType, isEmpty } from 'app/core/utils/util';
import { MessageQueueService, MESSAGE_TO, AnalyticsService, TRACKED_EVENT_LIST } from 'app/shared/services';

@Component({
    selector: 'pp-group-member-container',
    templateUrl: './group-member-container.component.html',
    styleUrls: ['./group-member-container.component.css']
})
export class GroupMemberContainerComponent implements OnInit, OnDestroy {
    private unsubscribe = new Subject<void>();
    private ascendSort = true;

    currentUserGroupId: string;
    groupMemberList: IGroupMember[] = [];
    useDisable = false;
    showLoading = false;
    errorMessage: string;
    emptyText$: Observable<string>;

    constructor(
        private translateService: TranslateService,
        private groupMemberDataService: GroupMemberDataService,
        private messageQueueService: MessageQueueService,
        private analyticsService: AnalyticsService,
    ) {}
    ngOnInit() {
        this.emptyText$ = this.translateService.get('COMMON.EMPTY');
        this.messageQueueService.receiveMessage(this.unsubscribe, MESSAGE_TO.USER_GROUP_SELECTED_USER_GROUP).subscribe((param: any) => {
            this.currentUserGroupId = param;
            if (this.isValidUserGroupId()) {
                this.getGroupMemberList();
            } else {
                this.groupMemberList = [];
                this.sendMessageCurrentGroupMemeberList([]);
            }
        });
        this.messageQueueService.receiveMessage(this.unsubscribe, MESSAGE_TO.PINPOINT_USER_ADD_USER).subscribe((userId: string) => {
            this.addGroupMember(userId);
        });
        this.messageQueueService.receiveMessage(this.unsubscribe, MESSAGE_TO.PINPOINT_USER_UPDATE_USER).subscribe((param: any) => {
            const memberInfo = param;
            let memberIndex = -1;
            let editMemberInfo;
            for (let i = 0 ; i < this.groupMemberList.length ; i++) {
                if (this.groupMemberList[i].memberId === memberInfo.userId) {
                    memberIndex = i;
                    editMemberInfo = {
                        name: memberInfo.name,
                        department: memberInfo.department,
                        number: this.groupMemberList[i].number,
                        memberId: this.groupMemberList[i].memberId,
                        userGroupId: this.groupMemberList[i].userGroupId
                    };
                    break;
                }
            }
            this.groupMemberList.splice(memberIndex, 1, editMemberInfo);
        });
        this.messageQueueService.receiveMessage(this.unsubscribe, MESSAGE_TO.PINPOINT_USER_REMOVE_USER).subscribe((userId: string) => {
            this.groupMemberList = this.groupMemberList.filter((member: IGroupMember) => {
                return member.memberId !== userId;
            });
        });
    }
    ngOnDestroy() {
        this.unsubscribe.next();
        this.unsubscribe.complete();
    }
    private isValidUserGroupId(): boolean {
        return this.currentUserGroupId !== '';
    }
    private getGroupMemberList(): void {
        this.showProcessing();
        this.groupMemberDataService.retrieve(this.currentUserGroupId).subscribe((data: IGroupMember[] | IServerErrorShortFormat) => {
            if (isThatType<IServerErrorShortFormat>(data, 'errorCode', 'errorMessage')) {
                this.sendMessageCurrentGroupMemeberList(this.getMemberIdList());
                this.errorMessage = data.errorMessage;
            } else {
                this.groupMemberList = data;
                this.sortGroupMemberList();
                this.sendMessageCurrentGroupMemeberList(this.getMemberIdList());
            }
            this.hideProcessing();
        }, (error: IServerErrorFormat) => {
            this.sendMessageCurrentGroupMemeberList(this.getMemberIdList());
            this.hideProcessing();
            this.errorMessage = error.exception.message;
        });
    }
    private addGroupMember(userId: string): void {
        this.groupMemberDataService.create(userId, this.currentUserGroupId).subscribe((response: IGroupMemberResponse | IServerErrorShortFormat) => {
            if (isThatType<IServerErrorShortFormat>(response, 'errorCode', 'errorMessage')) {
                this.errorMessage = response.errorMessage;
                this.hideProcessing();
            } else {
                this.doAfterAddAndRemoveAction(response);
            }
        }, (error: IServerErrorFormat) => {
            this.hideProcessing();
            this.errorMessage = error.exception.message;
        });
    }
    private getMemberIdList(): string[] {
        return this.groupMemberList.map((groupMember: IGroupMember): string => {
            return groupMember.memberId;
        });
    }
    private doAfterAddAndRemoveAction(response: IGroupMemberResponse): void {
        if (response.result === 'SUCCESS') {
            this.getGroupMemberList();
        } else {
            this.hideProcessing();
        }
    }
    private sortAscend(): void {
        this.groupMemberList.sort((a: IGroupMember, b: IGroupMember): number => {
            return a.name > b.name ? 1 : -1;
        });
    }
    private sortDescend(): void {
        this.groupMemberList.sort((a: IGroupMember, b: IGroupMember): number => {
            return a.name < b.name ? 1 : -1;
        });
    }
    private sortGroupMemberList() {
        if (this.ascendSort === true) {
            this.sortAscend();
        } else {
            this.sortDescend();
        }
    }
    private sendMessageCurrentGroupMemeberList(list: string[]): void {
        this.messageQueueService.sendMessage({
            to: MESSAGE_TO.GROUP_MEMBER_SET_CURRENT_GROUP_MEMBERS,
            param: list
        });
    }
    onRemoveGroupMember(id: string): void {
        this.showProcessing();
        this.groupMemberDataService.remove(id, this.currentUserGroupId).subscribe((response: IGroupMemberResponse) => {
            this.doAfterAddAndRemoveAction(response);
            this.analyticsService.trackEvent(TRACKED_EVENT_LIST.REMOVE_GROUP_MEMBER);
        }, (error: string) => {
            this.hideProcessing();
            this.errorMessage = error;
        });
    }
    onCloseErrorMessage(): void {
        this.errorMessage = '';
        this.sendMessageCurrentGroupMemeberList(this.getMemberIdList());
    }
    onSort(): void {
        if (this.isValidUserGroupId()) {
            this.ascendSort = !this.ascendSort;
            this.sortGroupMemberList();
            this.analyticsService.trackEvent(TRACKED_EVENT_LIST.SORT_GROUP_MEMBER_LIST);
        }
    }
    onReload(): void {
        this.getGroupMemberList();
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.RELOAD_GROUP_MEMBER_LIST);
    }
    private showProcessing(): void {
        this.useDisable = true;
        this.showLoading = true;
    }
    private hideProcessing(): void {
        this.useDisable = false;
        this.showLoading = false;
    }

    isEmpty(): boolean {
        return isEmpty(this.groupMemberList);
    }
}
