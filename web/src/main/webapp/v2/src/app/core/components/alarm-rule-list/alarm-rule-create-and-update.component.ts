import { Component, OnInit, Input, Output, EventEmitter, OnChanges, SimpleChanges } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';

import { IAlarmRule } from './alarm-rule-data.service';
import { filterObj } from 'app/core/utils/util';

export const enum NotificationType {
    ALL = 'all',
    EMAIL = 'email',
    SMS = 'sms'
}

export interface IAlarmForm {
    checkerName: string;
    userGroupId: string;
    threshold: number;
    type: string;
    notes: string;
}

@Component({
    selector: 'pp-alarm-rule-create-and-update',
    templateUrl: './alarm-rule-create-and-update.component.html',
    styleUrls: ['./alarm-rule-create-and-update.component.css']
})
export class AlarmRuleCreateAndUpdateComponent implements OnInit, OnChanges {
    @Input() checkerList: string[];
    @Input() userGroupList: string[];
    @Input() editAlarm: IAlarmRule;
    @Input() i18nLabel: {[key: string]: string};
    @Input() i18nFormGuide: {[key: string]: IFormFieldErrorType};
    @Output() outUpdateAlarm = new EventEmitter<IAlarmForm>();
    @Output() outCreateAlarm = new EventEmitter<IAlarmForm>();
    @Output() outClose = new EventEmitter<void>();

    alarmForm = new FormGroup({
        'checkerName': new FormControl('', [Validators.required]),
        'userGroupId': new FormControl('', [Validators.required]),
        'threshold': new FormControl(1, [Validators.required, Validators.min(1)]),
        'type': new FormControl('all'),
        'notes': new FormControl(''),
    });

    constructor() {}
    ngOnInit() {}
    ngOnChanges(changes: SimpleChanges) {
        const alarmChange = changes['editAlarm'];

        if (alarmChange && alarmChange.currentValue) {
            const formattedObj = filterObj((key: string) => Object.keys(this.alarmForm.controls).includes(key), alarmChange.currentValue);

            formattedObj.type = this.getTypeStr(this.editAlarm);
            this.alarmForm.reset(formattedObj);
        }
    }

    private getTypeStr({smsSend, emailSend}: IAlarmRule): string {
        return smsSend && emailSend ? 'all'
            : smsSend ? 'sms'
            : emailSend ? 'email'
            : 'none';
    }

    onCreateOrUpdate() {
        const alarm = this.alarmForm.value;

        this.editAlarm ? this.outUpdateAlarm.emit(alarm) : this.outCreateAlarm.emit(alarm);
        this.onClose();
    }

    onClose() {
        this.outClose.emit();
    }
}
