import { Component, OnInit, Input, Output, EventEmitter, OnChanges, SimpleChanges } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';

import { IAlarmRule } from './alarm-rule-data.service';
import { filterObj } from 'app/core/utils/util';
import { IWebhook } from 'app/shared/services';

export const enum NotificationType {
    ALL = 'all',
    EMAIL = 'email',
    SMS = 'sms',
    WEBHOOK = 'webhook'
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
    @Input() webhookEnable: boolean;
    @Input() webhookList: IWebhook[];
    @Input() checkedWebhookList: IWebhook['webhookId'][];
    @Input() showWebhookLoading: boolean;
    @Input() disableWebhookList: boolean;
    @Output() outUpdateAlarm = new EventEmitter<IAlarmForm>();
    @Output() outCreateAlarm = new EventEmitter<IAlarmForm>();
    @Output() outClose = new EventEmitter<void>();
    @Output() outShowHelp = new EventEmitter<{[key: string]: ICoordinate}>();
    @Output() outCheckWebhook = new EventEmitter<string>();

    alarmForm = new FormGroup({
        'checkerName': new FormControl('', [Validators.required]),
        'userGroupId': new FormControl('', [Validators.required]),
        'threshold': new FormControl(1, [Validators.required, Validators.min(1)]),
        'type': new FormControl('all'),
        'notes': new FormControl(''),
        'webhook': new FormControl({ disable: false }, [])
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

    private getTypeStr({smsSend, emailSend, webhookSend}: IAlarmRule): string {
        if (this.webhookEnable) {
            return smsSend && emailSend && webhookSend ? 'all'
                : smsSend ? 'sms'
                : emailSend ? 'email'
                : webhookSend ? 'webhook'
                : 'none';
        }

        return smsSend && emailSend ? 'all'
            : smsSend ? 'sms'
            : emailSend ? 'email'
            : 'none';
    }

    onCreateOrUpdate() {
        this.alarmForm.markAllAsTouched();

        if (this.alarmForm.invalid) {
            return;
        }

        const alarm = this.alarmForm.value;

        this.editAlarm ? this.outUpdateAlarm.emit(alarm) : this.outCreateAlarm.emit(alarm);
        this.onClose();
    }

    onClose() {
        this.outClose.emit();
    }

    onShowHelp(target: HTMLElement): void {
        const {left, top, width, height} = target.getBoundingClientRect();

        this.outShowHelp.emit({
            coord: {
                coordX: left + width / 2,
                coordY: top + height / 2
            }
        });
    }

    onCheckWebhook(webhook: string): void {
        this.outCheckWebhook.emit(webhook);
    }

    get isWebhookSelectDisable() {
        return (this.alarmForm.value.type === 'all' || this.alarmForm.value.type === 'webhook') ? false : true;
    }

    get webhookListScroll() {
        return this.showWebhookLoading || this.disableWebhookList || this.isWebhookSelectDisable ? 'hidden' : 'scroll';
    }
}
