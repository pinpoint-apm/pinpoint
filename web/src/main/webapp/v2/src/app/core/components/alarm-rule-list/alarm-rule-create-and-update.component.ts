import { Component, OnInit, Input, Output, EventEmitter, OnChanges, SimpleChanges } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';

export class Alarm {
    public applicationId: string;
    public ruleId: string;
    public smsSend: boolean;
    public emailSend: boolean;
    constructor(
        public checkerName: string,
        public userGroupId: string,
        public threshold: number,
        public type: string,
        public notes?: string
    ) {
        this.setTypeInternalStatus();
    }
    setTypeInternalStatus(): void {
        this.smsSend = this.type === 'all' || this.type === 'sms' ? true : false;
        this.emailSend = this.type === 'all' || this.type === 'email' ? true : false;
    }
}

@Component({
    selector: 'pp-alarm-rule-create-and-update',
    templateUrl: './alarm-rule-create-and-update.component.html',
    styleUrls: ['./alarm-rule-create-and-update.component.css']
})
export class AlarmRuleCreateAndUpdateComponent implements OnInit, OnChanges {
    @Input() showCreate: boolean;
    @Input() checkerList: string[];
    @Input() userGroupList: string[];
    @Input() i18nLabel: any;
    @Input() i18nGuide: { [key: string]: IFormFieldErrorType };
    @Input() editAlarm: Alarm = null;
    @Output() outUpdateAlarm: EventEmitter<Alarm> = new EventEmitter();
    @Output() outCreateAlarm: EventEmitter<Alarm> = new EventEmitter();
    @Output() outClose: EventEmitter<null> = new EventEmitter();
    newAlarmModel = new Alarm('', '', 1, 'all', '');
    alarmForm: FormGroup;
    title = 'Alarm';

    constructor() {}
    ngOnInit() {
        this.alarmForm = new FormGroup({
            'checkerName': new FormControl(this.newAlarmModel.checkerName, [
                Validators.required
            ]),
            'userGroupId': new FormControl(this.newAlarmModel.userGroupId, [
                Validators.required
            ]),
            'threshold': new FormControl(this.newAlarmModel.threshold, [
                Validators.required,
                Validators.min(1)
            ]),
            'type': new FormControl(this.newAlarmModel.type, []),
            'notes': new FormControl(this.newAlarmModel.notes, []),
            'applicationId': new FormControl(this.newAlarmModel.applicationId, []),
            'ruleId': new FormControl(this.newAlarmModel.ruleId, [])
        });
    }
    ngOnChanges(changes: SimpleChanges) {
        if (changes['showCreate'] && changes['showCreate'].currentValue === true) {
            this.setValue('', '', 1, 'all', '');
        }
        if (changes['editAlarm'] && changes['editAlarm'].currentValue) {
            this.setValue(
                this.editAlarm.checkerName,
                this.editAlarm.userGroupId,
                this.editAlarm.threshold,
                this.editAlarm.type,
                this.editAlarm.notes
            );
        }
    }
    private setValue(checkerName: string, userGroupId: string, threshold: number, type: string, notes: string): void {
        this.alarmForm.get('checkerName').setValue(checkerName);
        this.alarmForm.get('userGroupId').setValue(userGroupId);
        this.alarmForm.get('threshold').setValue(threshold);
        this.alarmForm.get('type').setValue(type);
        this.alarmForm.get('notes').setValue(notes);
    }
    onCreateOrUpdate() {
        const alarm = new Alarm(
            this.alarmForm.get('checkerName').value,
            this.alarmForm.get('userGroupId').value,
            this.alarmForm.get('threshold').value,
            this.alarmForm.get('type').value,
            this.alarmForm.get('notes').value
        );
        if (this.editAlarm) {
            this.outUpdateAlarm.emit(alarm);
        } else {
            this.outCreateAlarm.emit(alarm);
        }
        this.onClose();
    }
    onClose() {
        this.editAlarm = null;
        this.outClose.emit();
        this.alarmForm.reset();
    }
    get checkerName() {
        return this.alarmForm.get('checkerName');
    }
    get userGroupId() {
        return this.alarmForm.get('userGroupId');
    }
    get threshold() {
        return this.alarmForm.get('threshold');
    }
    get type() {
        return this.alarmForm.get('type');
    }
    get notes() {
        return this.alarmForm.get('notes');
    }
}
