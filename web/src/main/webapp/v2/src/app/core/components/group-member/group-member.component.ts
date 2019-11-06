import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { IGroupMember } from './group-member-data.service';

@Component({
    selector: 'pp-group-member',
    templateUrl: './group-member.component.html',
    styleUrls: ['./group-member.component.css']
})
export class GroupMemberComponent implements OnInit {
    @Input() groupMemberList: IGroupMember[];
    @Output() outRemove: EventEmitter<string> = new EventEmitter();
    private removeConformId = '';
    constructor() {}
    ngOnInit() {}
    onRemove(id: string): void {
        this.removeConformId = id;
    }
    onCancelRemove(): void {
        this.removeConformId = '';
    }
    onConfirmRemove(): void {
        this.outRemove.emit(this.removeConformId);
        this.removeConformId = '';
    }
    isRemoveTarget(id: string): boolean {
        return this.removeConformId === id;
    }
}
