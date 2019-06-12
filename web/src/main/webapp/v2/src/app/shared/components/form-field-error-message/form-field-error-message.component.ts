import { Component, OnInit, Input } from '@angular/core';
import { AbstractControl } from '@angular/forms';

@Component({
    selector: 'pp-form-field-error-message',
    templateUrl: './form-field-error-message.component.html',
    styleUrls: ['./form-field-error-message.component.css']
})
export class FormFieldErrorMessageComponent implements OnInit {
    @Input() control: AbstractControl;
    @Input() errorMessage: IFormFieldErrorType;

    constructor() {}
    ngOnInit() {}
    isFieldInValid(): boolean {
        return this.control && this.control.touched && this.control.invalid;
    }

    getErrorMessage(): string {
        return this.errorMessage[Object.keys(this.control.errors)[0] as keyof IFormFieldErrorType];
    }
}
