import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';

import { IAlarmRule } from './alarm-rule-data.service';

@Component({
    selector: 'pp-alarm-rule-list',
    templateUrl: './alarm-rule-list.component.html',
    styleUrls: ['./alarm-rule-list.component.css']
})
export class AlarmRuleListComponent implements OnInit {
    @Input() alarmRuleList: IAlarmRule[];
    @Input() webhookEnable: boolean;
    @Output() outRemove = new EventEmitter<string>();
    @Output() outEdit = new EventEmitter<string>();

    private removeConfirmId = '';

    constructor() {}
    ngOnInit() {}
    getNotificationType(emailSend: boolean, smsSend: boolean, webhookSend: boolean): string {
        const notificationTypes = [];

        if (emailSend) {
            notificationTypes.push('Email');
        }

        if (smsSend) {
            notificationTypes.push('SMS');
        }

        if (webhookSend && this.webhookEnable) {
            notificationTypes.push('Webhook');
        }

        if (notificationTypes.length === 0) {
            notificationTypes.push('None');
        }

        return notificationTypes.join(', ');
    }

    onRemove(ruleId: string): void {
        this.removeConfirmId = ruleId;
    }

    onEdit(ruleId: string): void {
        this.outEdit.emit(ruleId);
    }

    onCancelRemove(): void {
        this.removeConfirmId = '';
    }

    onConfirmRemove(): void {
        this.outRemove.emit(this.removeConfirmId);
        this.removeConfirmId = '';
    }

    isRemoveTarget(ruleId: string): boolean {
        return this.removeConfirmId === ruleId;
    }
}
