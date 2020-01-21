import { Component, OnInit, OnChanges, SimpleChanges, Input, Output, EventEmitter } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';

import { CustomFormValidatorService } from 'app/shared/services/custom-form-validator.service';
import { filterObj } from 'app/core/utils/util';

@Component({
    selector: 'pp-pinpoint-user-create-and-update',
    templateUrl: './pinpoint-user-create-and-update.component.html',
    styleUrls: ['./pinpoint-user-create-and-update.component.css']
})
export class PinpointUserCreateAndUpdateComponent implements OnInit, OnChanges {
    @Input() showCreate = false;
    @Input() i18nLabel: any;
    @Input() i18nGuide: { [key: string]: IFormFieldErrorType };
    @Input() minLength: any;
    @Input() editPinpointUser: IUserProfile = null;
    @Output() outUpdatePinpointUser = new EventEmitter<IUserProfile>();
    @Output() outCreatePinpointUser = new EventEmitter<IUserProfile>();
    @Output() outClose = new EventEmitter<void>();

    pinpointUserForm = new FormGroup({
        userId: new FormControl('', [
            Validators.required,
            CustomFormValidatorService.validate(/^[a-z0-9\_\-]{4,24}$/)
        ]),
        name: new FormControl('', [
            Validators.required,
            CustomFormValidatorService.validate(/^[\w\-\.ㄱ-ㅎ|ㅏ-ㅣ|가-힣]{1,30}$/)
        ]),
        department: new FormControl('', [
            CustomFormValidatorService.validate(/^[\w\.\-ㄱ-ㅎ|ㅏ-ㅣ|가-힣]{3,40}$/)
        ]),
        phoneNumber: new FormControl('', [
            CustomFormValidatorService.validate(/^[\d]{3,24}$/)
        ]),
        email: new FormControl('', [
            Validators.minLength(3),
            Validators.maxLength(60),
            CustomFormValidatorService.validate(/^[A-Za-z0-9\.\_\-]+@[A-Za-z0-9\.\-]+\.[A-Za-z]+$/)
        ])
    });

    constructor() {}
    ngOnInit() {}
    ngOnChanges(changes: SimpleChanges) {
        const userChange = changes['editPinpointUser'];

        if (userChange && userChange.currentValue) {
            const parsedObj = filterObj((key: string) => Object.keys(this.pinpointUserForm.controls).includes(key), userChange.currentValue);

            this.pinpointUserForm.reset(parsedObj);
            this.pinpointUserForm.get('userId').disable();
        }
    }

    onCreateOrUpdate(): void {
        const valueObj = Object.entries(this.pinpointUserForm.value).reduce((acc: IUserProfile, [k, v]: [string, string]) => {
            return {...acc, [k]: (v || '').trim()};
        }, {} as IUserProfile);

        this.editPinpointUser ? this.outUpdatePinpointUser.emit(valueObj) : this.outCreatePinpointUser.emit(valueObj);
        this.onClose();
    }

    onClose(): void {
        this.editPinpointUser = null;
        this.outClose.emit();
        this.pinpointUserForm.reset();
        this.pinpointUserForm.get('userId').enable();
    }
}
