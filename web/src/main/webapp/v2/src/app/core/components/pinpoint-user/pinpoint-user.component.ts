import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { IPinpointUser } from './pinpoint-user-data.service';

@Component({
    selector: 'pp-pinpoint-user',
    templateUrl: './pinpoint-user.component.html',
    styleUrls: ['./pinpoint-user.component.css']
})
export class PinpointUserComponent implements OnInit {
    @Input() pinpointUser: IPinpointUser;
    @Input() allowedUserEdit: boolean;
    @Input() isChecked = false;
    @Input() isEnabled = false;
    @Output() outRemove: EventEmitter<string> = new EventEmitter();
    @Output() outAddUser: EventEmitter<string> = new EventEmitter();
    @Output() outEditUser: EventEmitter<string> = new EventEmitter();
    private removeConformId = '';
    private selectedPinpointUser: string;

    constructor() {}
    ngOnInit() {}
    onRemove(): void {
        this.removeConformId = this.pinpointUser.userId;
    }
    onEdit(): void {
        this.outEditUser.emit(this.pinpointUser.userId);
    }
    onAddUser(): void {
        if (this.isEnableUser()) {
            this.outAddUser.emit(this.pinpointUser.userId);
        }
    }
    onCancelRemove(): void {
        this.removeConformId = '';
    }
    onConfirmRemove(): void {
        this.outRemove.emit(this.removeConformId);
        this.removeConformId = '';
    }
    isRemoveTarget(): boolean {
        return this.removeConformId === this.pinpointUser.userId;
    }
    isSelected(): boolean {
        return this.selectedPinpointUser === this.pinpointUser.userId;
    }
    isEnableUser(): boolean {
        return this.isEnabled && !this.isChecked;
    }
}
