import { Component, OnInit, OnChanges, SimpleChanges, Input, Output, EventEmitter, ElementRef, ViewChild, AfterViewInit } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import intlTelInput from 'intl-tel-input';

import { CustomFormValidatorService } from 'app/shared/services/custom-form-validator.service';
import { filterObj } from 'app/core/utils/util';

@Component({
    selector: 'pp-pinpoint-user-create-and-update',
    templateUrl: './pinpoint-user-create-and-update.component.html',
    styleUrls: ['./pinpoint-user-create-and-update.component.css']
})
export class PinpointUserCreateAndUpdateComponent implements OnInit, OnChanges, AfterViewInit {
    @ViewChild('telInput', {static: false}) telInputRef: ElementRef;
    @Input() i18nLabel: any;
    @Input() i18nGuide: { [key: string]: IFormFieldErrorType };
    @Input() minLength: any;
    @Input() userInfo: IUserProfile;
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
        phoneCountryCode: new FormControl('82'),
        phoneNumber: new FormControl('', [
            CustomFormValidatorService.validate(/^[\d-]+$/),
        ]),
        email: new FormControl('', [
            Validators.minLength(3),
            Validators.maxLength(60),
            CustomFormValidatorService.validate(/^[A-Za-z0-9\.\_\-]+@[A-Za-z0-9\.\-]+\.[A-Za-z]+$/)
        ])
    });

    telIti: any;

    constructor() {}
    ngOnInit() {}
    ngOnChanges(changes: SimpleChanges) {
        const userChange = changes['userInfo'];

        if (userChange && userChange.currentValue) {
            const parsedObj = filterObj((key: string) => {
                return Object.keys(this.pinpointUserForm.controls).includes(key) && !!userChange.currentValue[key];
            }, userChange.currentValue);

            this.pinpointUserForm.patchValue(parsedObj);
        }
    }

    ngAfterViewInit() {
        this.telIti = intlTelInput(this.telInputRef.nativeElement, {
            autoPlaceholder: 'off',
            formatOnDisplay: false,
            separateDialCode: true,
            preferredCountries: ['kr'],
            utilsScript: 'assets/scripts/utils.js'
        });

        if (this.userInfo && this.userInfo.phoneCountryCode && this.userInfo.phoneNumber) {
            this.telIti.setNumber(`+${this.userInfo.phoneCountryCode}${this.userInfo.phoneNumber}`);
        }
    }

    onCreateOrUpdate(): void {
        const valueObj = Object.entries(this.pinpointUserForm.value).reduce((acc: IUserProfile, [k, v]: [string, string]) => {
            const value = k === 'phoneNumber' ? v.replace(/-/g, '')
                : k === 'phoneCountryCode' && this.pinpointUserForm.get('phoneNumber').value === '' ? ''
                : v;

            return {...acc, [k]: (value.toString() || '').trim()};
        }, {} as IUserProfile);


        this.userInfo ? this.outUpdatePinpointUser.emit(valueObj) : this.outCreatePinpointUser.emit(valueObj);
        this.onClose();
    }

    onClose(): void {
        this.outClose.emit();
    }

    onKeyup(): void {
        this.updateValidator();
    }

    onCountryChange(): void {
        this.pinpointUserForm.patchValue({
            phoneCountryCode: this.telIti.getSelectedCountryData().dialCode
        });
        this.updateValidator();
    }

    private updateValidator(): void {
        this.pinpointUserForm.controls['phoneNumber'].setValidators([
            CustomFormValidatorService.validate(/^[\d-]+$/),
            CustomFormValidatorService.validate(this.telIti.isValidNumber())
        ]);
        this.pinpointUserForm.controls['phoneNumber'].updateValueAndValidity();
    }
}
