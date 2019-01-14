import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';

@Component({
    selector: 'pp-alarm-rule-list',
    templateUrl: './alarm-rule-list.component.html',
    styleUrls: ['./alarm-rule-list.component.css']
})
export class AlarmRuleListComponent implements OnInit {
    @Input() alarmRuleList: any;
    @Output() outRemove: EventEmitter<string> = new EventEmitter();
    @Output() outEdit: EventEmitter<string> = new EventEmitter();
    private removeConformId = '';
    constructor() { }
    ngOnInit() {}
    getNotificationType(emailSend: boolean, smsSend: boolean): string {
        const returnStr = [];
        if (emailSend === false && smsSend === false) {
            return 'None';
        } else {
            if (emailSend) {
                returnStr.push('Email');
            }
            if (smsSend) {
                returnStr.push('SMS');
            }
            return returnStr.join(',');
        }
    }
    onRemove(ruleId: string): void {
        this.removeConformId = ruleId;
    }
    onEdit(ruleId: string): void {
        this.outEdit.emit(ruleId);
    }
    onCancelRemove(): void {
        this.removeConformId = '';
    }
    onConfirmRemove(): void {
        this.outRemove.emit(this.removeConformId);
        this.removeConformId = '';
    }
    isRemoveTarget(ruleId: string): boolean {
        return this.removeConformId === ruleId;
    }
}
