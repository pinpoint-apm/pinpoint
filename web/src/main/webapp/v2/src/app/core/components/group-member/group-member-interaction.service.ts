import { Injectable } from '@angular/core';
import { Subject, Observable } from 'rxjs';

@Injectable()
export class GroupMemberInteractionService {
    private outChangeGroupMember = new Subject<string[]>();
    onChangeGroupMember$: Observable<string[]>;

    constructor() {
        this.onChangeGroupMember$ = this.outChangeGroupMember.asObservable();
    }
    setChangeGroupMember(groupMemberList: string[]): void {
        this.outChangeGroupMember.next(groupMemberList);
    }
}

