import { Component, OnInit, OnChanges, SimpleChanges, Input, Output, EventEmitter, AfterViewChecked, ViewChild, ElementRef } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';

import { CustomFormValidatorService } from 'app/shared/services/custom-form-validator.service';

export class PinpointUser {
    constructor(
        public userId: string,
        public name: string,
        public phoneNumber: string,
        public email: string,
        public department?: string
    ) {}
}
@Component({
    selector: 'pp-pinpoint-user-create-and-update',
    templateUrl: './pinpoint-user-create-and-update.component.html',
    styleUrls: ['./pinpoint-user-create-and-update.component.css']
})
export class PinpointUserCreateAndUpdateComponent implements OnInit, OnChanges, AfterViewChecked {
    @ViewChild('newUserGroupName') userGroupInput: ElementRef;
    @Input() showCreate = false;
    @Input() i18nLabel: any;
    @Input() i18nGuide: { [key: string]: IFormFieldErrorType };
    @Input() minLength: any;
    @Input() editPinpointUser: PinpointUser = null;
    @Output() outUpdatePinpointUser: EventEmitter<PinpointUser> = new EventEmitter();
    @Output() outCreatePinpointUser: EventEmitter<PinpointUser> = new EventEmitter();
    @Output() outClose: EventEmitter<null> = new EventEmitter();
    newUserModel = new PinpointUser('', '', '', '', '');
    pinpointUserForm: FormGroup;
    title = 'Pinpoint User';
    constructor() {}
    ngOnInit() {
        this.pinpointUserForm = new FormGroup({
            'userId': new FormControl(this.newUserModel.userId, [
                Validators.required,
                CustomFormValidatorService.validate(/^[a-z0-9-\_\-]{4,24}$/)
            ]),
            'name': new FormControl(this.newUserModel.name, [
                Validators.required,
                CustomFormValidatorService.validate(/^[\w\-\.ㄱ-ㅎ|ㅏ-ㅣ|가-힣]{1,30}$/)
            ]),
            'phoneNumber': new FormControl(this.newUserModel.phoneNumber, [
                CustomFormValidatorService.validate(/^[\d]{3,24}$/)
            ]),
            'email': new FormControl(this.newUserModel.email, [
                Validators.minLength(3),
                Validators.maxLength(60),
                CustomFormValidatorService.validate(/^[A-Za-z0-9\.\_\-]+@[A-Za-z0-9\.\-]+\.[A-Za-z]+$/)
            ]),
            'department': new FormControl(this.newUserModel.department, [
                CustomFormValidatorService.validate(/^[\w\.\-ㄱ-ㅎ|ㅏ-ㅣ|가-힣]{3,40}$/)
            ])
        });
    }
    ngOnChanges(changes: SimpleChanges) {
        if (changes['editPinpointUser'] && changes['editPinpointUser'].currentValue) {
            this.pinpointUserForm.get('userId').setValue(this.editPinpointUser.userId);
            this.pinpointUserForm.get('name').setValue(this.editPinpointUser.name);
            this.pinpointUserForm.get('phoneNumber').setValue(this.editPinpointUser.phoneNumber);
            this.pinpointUserForm.get('email').setValue(this.editPinpointUser.email);
            this.pinpointUserForm.get('department').setValue(this.editPinpointUser.department);
            this.pinpointUserForm.get('userId').disable();
        }
    }
    ngAfterViewChecked() {}
    onCreateOrUpdate(): void {
        const pinpointUser = new PinpointUser(
            (this.pinpointUserForm.get('userId').value || '').trim(),
            (this.pinpointUserForm.get('name').value || '').trim(),
            (this.pinpointUserForm.get('phoneNumber').value || '').trim(),
            (this.pinpointUserForm.get('email').value || '').trim(),
            (this.pinpointUserForm.get('department').value || '').trim()
        );
        if (this.editPinpointUser) {
            this.outUpdatePinpointUser.emit(pinpointUser);
        } else {
            this.outCreatePinpointUser.emit(pinpointUser);
        }
        this.onClose();
    }
    onClose(): void {
        this.editPinpointUser = null;
        this.outClose.emit();
        this.pinpointUserForm.reset();
        this.pinpointUserForm.get('userId').enable();
    }
    get userId() {
        return this.pinpointUserForm.get('userId');
    }
    get name() {
        return this.pinpointUserForm.get('name');
    }
    get department() {
        return this.pinpointUserForm.get('department');
    }
    get phoneNumber() {
        return this.pinpointUserForm.get('phoneNumber');
    }
    get email() {
        return this.pinpointUserForm.get('email');
    }
}
