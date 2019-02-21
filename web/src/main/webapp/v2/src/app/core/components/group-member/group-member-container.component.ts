import { Component, OnInit } from '@angular/core';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { UserGroupInteractionService } from 'app/core/components/user-group/user-group-interaction.service';
import { PinpointUserInteractionService } from 'app/core/components/pinpoint-user/pinpoint-user-interaction.service';
import { GroupMemberInteractionService } from './group-member-interaction.service';
import { GroupMemberDataService, IGroupMember, IGroupMemberResponse } from './group-member-data.service';
import { isThatType } from 'app/core/utils/util';

@Component({
    selector: 'pp-group-member-container',
    templateUrl: './group-member-container.component.html',
    styleUrls: ['./group-member-container.component.css']
})
export class GroupMemberContainerComponent implements OnInit {
    private unsubscribe: Subject<null> = new Subject();
    private ascendSort = true;
    currentUserGroupId: string;
    groupMemberList: IGroupMember[] = [];
    useDisable = false;
    showLoading = false;
    errorMessage: string;

    constructor(
        private groupMemberDataService: GroupMemberDataService,
        private groupMemberInteractionService: GroupMemberInteractionService,
        private userGroupInteractionService: UserGroupInteractionService,
        private pinpointUserInteracionService: PinpointUserInteractionService
    ) {}
    ngOnInit() {
        this.userGroupInteractionService.onSelect$.pipe(
            takeUntil(this.unsubscribe)
        ).subscribe((id: string) => {
            this.currentUserGroupId = id;
            if (this.isValidUserGroupId()) {
                this.getGroupMemberList();
            } else {
                this.groupMemberList = [];
                this.groupMemberInteractionService.setChangeGroupMember([]);
            }
        });
        this.pinpointUserInteracionService.onAdd$.pipe(
            takeUntil(this.unsubscribe)
        ).subscribe((userId: string) => {
            this.addGroupMember(userId);
        });
        this.pinpointUserInteracionService.onUpdate$.pipe(
            takeUntil(this.unsubscribe)
        ).subscribe((memberInfo: any) => {
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
    }
    private isValidUserGroupId(): boolean {
        return this.currentUserGroupId !== '';
    }
    private getGroupMemberList(): void {
        this.showProcessing();
        this.groupMemberDataService.retrieve(this.currentUserGroupId).subscribe((data: IGroupMember[] | IServerErrorShortFormat) => {
            if (isThatType<IServerErrorShortFormat>(data, 'errorCode', 'errorMessage')) {
                this.groupMemberInteractionService.setChangeGroupMember(this.getMemberIdList());
                this.errorMessage = data.errorMessage;
            } else {
                this.groupMemberList = data;
                this.sortGroupMemberList();
                this.groupMemberInteractionService.setChangeGroupMember(this.getMemberIdList());
            }
            this.hideProcessing();
        }, (error: IServerErrorFormat) => {
            this.groupMemberInteractionService.setChangeGroupMember(this.getMemberIdList());
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
    onRemoveGroupMember(id: string): void {
        this.showProcessing();
        this.groupMemberDataService.remove(id, this.currentUserGroupId).subscribe((response: IGroupMemberResponse) => {
            this.doAfterAddAndRemoveAction(response);
        }, (error: string) => {
            this.hideProcessing();
            this.errorMessage = error;
        });
    }
    onCloseErrorMessage(): void {
        this.errorMessage = '';
        this.groupMemberInteractionService.setChangeGroupMember(this.getMemberIdList());
    }
    onSort(): void {
        if (this.isValidUserGroupId()) {
            this.ascendSort = !this.ascendSort;
            this.sortGroupMemberList();
        }
    }
    onReload(): void {
        this.getGroupMemberList();
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
