import { Component, OnInit, Input, Output, EventEmitter, OnChanges, SimpleChanges } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';

import { filterObj } from 'app/core/utils/util';

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
    selector: 'pp-webhook-list-create-and-update',
    templateUrl: './webhook-list-create-and-update.component.html',
    styleUrls: ['./webhook-list-create-and-update.component.css']
})
export class WebhookListCreateAndUpdateComponent implements OnInit, OnChanges {
    @Input() checkerList: string[];
    @Input() userGroupList: string[];
    @Input() i18nLabel: {[key: string]: string};
    @Input() i18nFormGuide: {[key: string]: IFormFieldErrorType};
    @Input() webhookEnable: boolean;
    @Output() outUpdateAlarm = new EventEmitter<IAlarmForm>();
    @Output() outCreateAlarm = new EventEmitter<IAlarmForm>();
    @Output() outClose = new EventEmitter<void>();
    @Output() outShowHelp = new EventEmitter<{[key: string]: ICoordinate}>();

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
        // const alarmChange = changes['editAlarm'];

        // if (alarmChange && alarmChange.currentValue) {
        //     const formattedObj = filterObj((key: string) => Object.keys(this.alarmForm.controls).includes(key), alarmChange.currentValue);

        //     formattedObj.type = this.getTypeStr(this.editAlarm);
        //     this.alarmForm.reset(formattedObj);
        // }
    }

    onCreateOrUpdate() {
        // this.alarmForm.markAllAsTouched();

        // if (this.alarmForm.invalid) {
        //     return;
        // }

        // const alarm = this.alarmForm.value;

        // this.editAlarm ? this.outUpdateAlarm.emit(alarm) : this.outCreateAlarm.emit(alarm);
        // this.onClose();
    }

    onClose() {
        this.outClose.emit();
    }

    onShowHelp(target: HTMLElement): void {
        // const {left, top, width, height} = target.getBoundingClientRect();

        // this.outShowHelp.emit({
        //     coord: {
        //         coordX: left + width / 2,
        //         coordY: top + height / 2
        //     }
        // });
    }
}
