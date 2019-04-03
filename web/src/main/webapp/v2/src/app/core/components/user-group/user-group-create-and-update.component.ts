import { Component, OnInit, AfterViewChecked, Input, Output, EventEmitter } from '@angular/core';
import { FormControl, FormGroup, Validators, ValidatorFn, AbstractControl } from '@angular/forms';

function userGroupNameValidator(valueRe: RegExp): ValidatorFn {
    return (control: AbstractControl): {[key: string]: any} | null => {
        const invalid = !valueRe.test(control.value);
        return invalid ? {'valueRule': {value: control.value}} : null;
    };
}
@Component({
    selector: 'pp-user-group-create-and-update',
    templateUrl: './user-group-create-and-update.component.html',
    styleUrls: ['./user-group-create-and-update.component.css']
})
export class UserGroupCreateAndUpdateComponent implements OnInit, AfterViewChecked {
    @Input() showCreate = false;
    @Input() i18nLabel: { [key: string]: string };
    @Input() i18nGuide: { [key: string]: IFormFieldErrorType };
    @Input() minLength: number;
    @Output() outCreateUserGroup: EventEmitter<string> = new EventEmitter();
    @Output() outClose: EventEmitter<null> = new EventEmitter();
    newUserGroupModel = '';
    userGroupForm: FormGroup;
    title = 'User Group';
    constructor() {}
    ngOnInit() {
        this.userGroupForm = new FormGroup({
            'userGroupName': new FormControl(this.newUserGroupModel, [
                Validators.required,
                userGroupNameValidator(/^[\w\-]{4,30}$/)
            ])
        });
    }
    ngAfterViewChecked() {}
    onCreateOrUpdate(): void {
        this.outCreateUserGroup.emit(this.userGroupForm.get('userGroupName').value);
        this.onClose();
    }
    onClose(): void {
        this.outClose.emit();
        this.userGroupForm.reset();
    }
    get userGroupName() {
        return this.userGroupForm.get('userGroupName');
    }
}
