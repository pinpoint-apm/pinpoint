import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { IUserGroup } from './user-group-data.service';

@Component({
    selector: 'pp-user-group',
    templateUrl: './user-group.component.html',
    styleUrls: ['./user-group.component.css']
})
export class UserGroupComponent implements OnInit {
    @Input() groupList: IUserGroup[];
    @Output() outRemove: EventEmitter<string> = new EventEmitter();
    @Output() outSelected: EventEmitter<string> = new EventEmitter();
    private removeConformId = '';
    private selectedUserGroup: string;
    constructor() {}
    ngOnInit() {}
    onRemove(id: string): void {
        this.removeConformId = id;
    }
    onSelect($event: MouseEvent, userGroup: IUserGroup): void {
        if ($event.target['tagName'].toLowerCase() === 'button') {
            return;
        }
        if (this.selectedUserGroup !== userGroup.id) {
            this.selectedUserGroup = userGroup.id;
            this.outSelected.emit(this.selectedUserGroup);
        }
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
    isSelected(id: string): boolean {
        return this.selectedUserGroup === id;
    }
}
